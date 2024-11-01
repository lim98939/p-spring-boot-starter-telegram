package dev.voroby.springframework.telegram.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.drinkless.tdlib.TdApi;
import dev.voroby.springframework.telegram.client.TelegramClient;
import dev.voroby.springframework.telegram.client.templates.response.Response;
import dev.voroby.springframework.telegram.entity.ProxyData;
import dev.voroby.springframework.telegram.entity.ProxyVO;
import dev.voroby.springframework.telegram.utils.AesUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service @Slf4j
public class TelegramClientService {

    private final TelegramClient telegramClient;

    private final Deque<TdApi.Message> messages = new ConcurrentLinkedDeque<>();

    public static int PROXY_COUNT = 7;

    private final ProxyDataService proxyDataService;

    private String sendMsg = "";

    public static Boolean SCHEDULED= false;

    public TelegramClientService(@Lazy TelegramClient telegramClient, ProxyDataService proxyDataService) {
        this.telegramClient = telegramClient;
        this.proxyDataService = proxyDataService;
    }

//    public void putMessage(TdApi.Message msg) {
//        messages.addLast(msg);
//    }

//    @Scheduled(fixedDelay = 1000)
//    private void handleMessages() {
//        for (int i = 0; i < 100; i++) {
//            TdApi.Message message = messages.pollFirst();
//            if (message == null) {
//                break;
//            }
//            TdApi.MessageContent content = message.content;
//            if (content instanceof TdApi.MessageText mt) {
//                Response<TdApi.Chat> getChatResponse = telegramClient.send(new TdApi.GetChat(message.chatId));
//                ofNullable(getChatResponse.object()).ifPresentOrElse(
//                        chat -> log.info("Incoming text message:\n[\n\ttitle: {},\n\tmessage: {}\n]", chat.title, mt.text.text),
//                        () -> log.error(getChatResponse.error().message)
//                );
//            }
//        }
//    }


    private List<ProxyVO> getProxies() throws ExecutionException, InterruptedException {
        return getProxyVOS(telegramClient, log);
    }

    private List<ProxyVO> getProxyVOS(TelegramClient telegramClient, Logger log) throws InterruptedException, ExecutionException {
        // 获取所有代理
        TdApi.Proxies proxies = telegramClient.sendAsync(new TdApi.GetProxies()).get().object();

        // 使用 CompletableFuture 并行处理代理信息
        List<CompletableFuture<ProxyVO>> futureList = new ArrayList<>();

        List<TdApi.Proxy> badProxies = new ArrayList<>();

        for (TdApi.Proxy proxy : proxies.proxies) {
            CompletableFuture<ProxyVO> future = CompletableFuture.supplyAsync(() -> {
                TdApi.HttpUrl httpUrl = null;
                TdApi.Seconds seconds = null;

                try {
                    // 异步获取 Proxy 链接
                    httpUrl = telegramClient.sendAsync(new TdApi.GetProxyLink(proxy.id)).get().object();

                    // 异步 Ping 代理
                    seconds = telegramClient.sendAsync(new TdApi.PingProxy(proxy.id)).get().object();

                    // 如果 seconds 为空，直接跳过（不加入集合）
                    if (seconds == null) {
                        log.warn("PingProxy returned null for proxy ID: " + proxy.server);
                        badProxies.add(proxy);
                        return null; // 返回 null 代表不加入集合
                    }

                } catch (Exception e) {
                    // 捕获异常并记录日志，不处理该代理
                    log.error("Error while processing proxy ID: " + proxy.server, e);
                    badProxies.add(proxy);
                    return null; // 返回 null 代表不加入集合
                }

                // 创建 ProxyVO 并返回
                return new ProxyVO(proxy, seconds, httpUrl);
            });

            futureList.add(future); // 将每个异步任务加入到列表中
        }

        // 等待所有任务完成，并过滤掉返回 null 的结果
         List<ProxyVO> noNullProxies = futureList.stream()
                .map(CompletableFuture::join) // 等待每个 CompletableFuture 完成
                .filter(Objects::nonNull)     // 过滤掉 null 值
                .collect(Collectors.toList());

        if (!badProxies.isEmpty()) {
            StringBuilder badProxiesStr = new StringBuilder("\uD83D\uDEAB有误代理信息:\n\n");
            for (TdApi.Proxy proxy : badProxies) {
                badProxiesStr.append("\uD83D\uDD3A").append(" - ").append("【")
                        .append(proxy.server).append("\t | ")
                        .append(proxy.port).append("】").append("\n");
            }
            String finalMsg = badProxiesStr.toString();
            sendMsg += finalMsg + "\n\n";
        }

        return noNullProxies;
    }

    public static List<ProxyVO> getRandom7ProxyVOS(List<ProxyVO> proxyVOS) {
        // 如果集合大于7，随机打乱集合并取出前7个元素
        if (proxyVOS.size() > PROXY_COUNT) {
            Collections.shuffle(proxyVOS);  // 随机打乱集合
            return new ArrayList<>(proxyVOS.subList(0, PROXY_COUNT));  // 取出前7个元素
        } else {
            // 否则，返回原集合
            return proxyVOS;
        }
    }

    private TdApi.SendMessage sendMessageQuery(Long chatId, String msg) {
        var content = new TdApi.InputMessageText();
        var formattedText = new TdApi.FormattedText();
        formattedText.text = msg;
        content.text = formattedText;
        return new TdApi.SendMessage(chatId, 0, null, null, null, content);
    }

    // 使用 cron 表达式设定每天北京时间上午 8:00 触发任务
    @Scheduled(cron = "0 0 8 * * ?", zone = "Asia/Shanghai")
//    @Scheduled(fixedDelay = 60000 * 5)
    private void reSetProxies() {
        if (!SCHEDULED) {
            System.out.println("Selected False");
            return;
        }
        // 输出每个 ProxyVO 的详细信息

        // 定义中文日期格式
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss", Locale.CHINESE);

        // 将 Date 转换为中文格式的字符串
        String chineseDate = sdf.format(new Date());
        sendMsg = "==="+chineseDate+"===\n";
        try {
            List<ProxyVO> proxyVOS = getRandom7ProxyVOS(getProxies());
            if (proxyVOS.isEmpty()){
                return;
            }
            this.proxyDataService.deleteALL();
            // 输出每个 ProxyVO 的详细信息
            System.out.println("Selected ProxyVO Details:");
            StringBuilder msg = new StringBuilder("\uD83D\uDCF1代理信息:\n\n");
            int index = 1;
            for (ProxyVO proxyVO : proxyVOS) {
                log.info(proxyVO.toString());  // 输出 ProxyVO 对象的详细信息
                ProxyData proxyData = new ProxyData();
                String[] serverStrS = proxyVO.server.split("\\.");
                proxyData.setProxyname("line connection - "+serverStrS[0]+"#"+serverStrS[3]);
                proxyData.setPoxyport(proxyVO.port);
                proxyData.setPoxyaddress(proxyVO.server);
                String type = "MTP";
                if (proxyVO.type instanceof TdApi.ProxyTypeSocks5){
                    type = "SOCKS5";
                    proxyData.setUsername(((TdApi.ProxyTypeSocks5) proxyVO.type).username);
                    proxyData.setPassword(((TdApi.ProxyTypeSocks5) proxyVO.type).password);
                }else {
                    proxyData.setPoxysecret(((TdApi.ProxyTypeMtproto) proxyVO.type).secret);
                }
                proxyData.setPoxytype(type);
                proxyData.setProxyimg(1);
                proxyData.setPoxyid(index);
                proxyData.setCreatedAt(new Date());
                proxyData.setUpdatedAt(new Date());

                this.proxyDataService.insert(proxyData);
                String applyBtn = "➖  "+proxyVO.httpUrl.url;
                msg.append(index).append(" - ").append("【")
                        .append(proxyVO.server).append("\t | ")
                        .append(proxyVO.port).append("】").append("\n")
                        .append(applyBtn).append("\n");
                index ++;
            }

            String finalMsg = msg.toString();
            sendMsg += finalMsg;
            telegramClient.sendAsync(sendMessageQuery(1544849672L, sendMsg));

        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

//    @Scheduled(fixedDelay = 60000)
    private void test(){
        System.out.println("test");
        telegramClient.sendAsync(sendMessageQuery(1544849672L, "Test"));
    }

}
