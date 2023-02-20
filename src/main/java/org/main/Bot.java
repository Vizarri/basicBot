package org.main;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.main.NonCommand.NonCommand;
import org.main.NonCommand.Settings;
import org.main.commands.operations.AllCommand;
import org.main.commands.operations.PlusCommand;
import org.main.commands.service.StartCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.helpCommand.HelpCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
public class Bot extends TelegramLongPollingCommandBot {

    private Logger logger = LoggerFactory.getLogger(Bot.class);
    private final String BOT_NAME;
    private final String BOT_TOKEN;

    @Getter
    //Настройки по умолчанию
    private static final Settings defaultSettings = new Settings(1, 15, 1);

    //Класс для обработки сообщений, не являющихся командой
    private final NonCommand nonCommand;

    @Getter
    /**
     * Настройки файла для разных пользователей. Ключ - уникальный id чата
     */
    private static Map<Long, Settings> userSettings;

    public Bot(String botName, String botToken) {
        super();
        logger.debug("Конструктор суперкласса отработал");
        this.BOT_NAME = botName;
        this.BOT_TOKEN = botToken;
        logger.debug("Имя и токен присвоены");

        this.nonCommand = new NonCommand();
        logger.debug("Класс обработки сообщения, не являющегося командой, создан");

        register(new StartCommand("start", "Старт"));
        logger.debug("Команда start создана");

        register(new PlusCommand("plus", "Сложение"));
        logger.debug("Команда plus создана");

        register(new AllCommand("all", "4 действия"));
        logger.debug("Команда all создана");

        register(new HelpCommand());
        logger.debug("Команда help создана");

        userSettings = new HashMap<>();
        logger.info("Бот создан!");
    }
    /**
     * Возвращает имя бота
     */
    @Override
    public String getBotUsername() {
        return BOT_NAME;
    }

    /**
     * @param update
     */
    @Override
    public void processNonCommandUpdate(Update update) {
        Message msg = update.getMessage();
        Long chatId = msg.getChatId();
        String userName = getUserName(msg);

        String answer = nonCommand.nonCommandExecute(chatId, userName, msg.getText());
        setAnswer(chatId, userName, answer);
    }

    /**
     * Возвращает токен бота
     */
    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }

    /**
     *
     */
    @Override
    public void onRegister() {
        super.onRegister();
    }

    /**
     * @param updates
     */
    @Override
    public void onUpdatesReceived(List<Update> updates) {
        super.onUpdatesReceived(updates);
    }

    /**
     * Получение настроек по id чата. Если ранее для этого чата в ходе сеанса работы бота настройки не были установлены, используются настройки по умолчанию
     */
    public static Settings getUserSettings(Long chatId) {
        Map<Long, Settings> userSettings = Bot.getUserSettings();
        Settings settings = userSettings.get(chatId);
        if (settings == null) {
            return defaultSettings;
        }
        return settings;
    }

    /**
     * Формирование имени пользователя
     * @param msg сообщение
     */
    private String getUserName(Message msg) {
        User user = msg.getFrom();
        String userName = user.getUserName();
        return (userName != null) ? userName : String.format("%s %s", user.getLastName(), user.getFirstName());
    }

    /**
     * Отправка ответа
     * @param chatId id чата
     * @param userName имя пользователя
     * @param text текст ответа
     */
    private void setAnswer(Long chatId, String userName, String text) {
        SendMessage answer = new SendMessage();
        answer.setText(text);
        answer.setChatId(chatId.toString());
        try {
            execute(answer);
        } catch (TelegramApiException e) {
            //логируем сбой Telegram Bot API, используя userName
        }
    }
}
