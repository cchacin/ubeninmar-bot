package org.acme;

import java.util.List;
import java.util.function.Function;
import org.acme.model.ProcessedLink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public record BeautyByUbeBot(
        TelegramClient client,
        Function<String, List<ProcessedLink>> processMessage,
        Function<List<ProcessedLink>, List<String>> formatResponses)
        implements LongPollingSingleThreadUpdateConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(BeautyByUbeBot.class);

    @Override
    public void consume(Update update) {
        var message = update.getMessage();
        if (update.hasMessage() && message.hasText()) {
            var messageText = message.getText();
            var chatId = message.getChatId().toString();

            LOGGER.info("Message received from chat {}: {}", chatId, messageText);

            if ("/start".equals(messageText)) {
                sendResponse(
                        chatId,
                        """
                        👋🏼️ Hello %s 👋🏼️
                        Send me your amazon links
                        🙏🏼️ thanks for the support 🙏🏼️
                        """
                                .formatted(message.getChat().getFirstName()));
                return;
            }

            // Process the message for Amazon links
            var processedLinks = processMessage.apply(messageText);

            if (!processedLinks.isEmpty()) {
                // Send responses for each successfully processed link
                var responses = formatResponses.apply(processedLinks);

                for (var response : responses) {
                    sendResponse(chatId, response);
                }

                LOGGER.info("Processed {} Amazon links for chat {}", processedLinks.size(), chatId);
            } else {
                LOGGER.debug("No Amazon links found in message from chat {}", chatId);
            }
        }
    }

    private void sendResponse(String chatId, String responseText) {
        var sendMessage = SendMessage.builder().chatId(chatId).text(responseText).build();

        try {
            client.execute(sendMessage);
            LOGGER.debug("Response sent to chat {}: {}", chatId, responseText);
        } catch (TelegramApiException e) {
            LOGGER.error("Failed to send response to chat {}: {}", chatId, responseText, e);
        }
    }
}
