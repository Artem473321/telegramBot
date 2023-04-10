package org.example;

import lombok.extern.log4j.Log4j;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.util.*;

@Log4j
public class SimpleEchoBot extends TelegramLongPollingBot implements Messages,Path {

    private static final String[][] LIST_OF_PROFECION = {{"Фізика", "Математика", "Фізкультура"},
            {"Біологія", "Хімія", "Англійська мова"}};
    private static final Map<String,String[]> PROFECION_TEXTS = new HashMap<>();

    private InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
    private SendPhoto sendPhoto = new SendPhoto();
    private SendMessage message = new SendMessage();

    private Long userId;

    static {
        PROFECION_TEXTS.put(LIST_OF_PROFECION[0][0], new String[]{TEACHER_OF_PHISICS, PHOTO_PHYSICS});
        PROFECION_TEXTS.put(LIST_OF_PROFECION[0][1],new String[]{TEACHER_OF_MATH,PHOTO_MATH});
        PROFECION_TEXTS.put(LIST_OF_PROFECION[0][2],new String[]{TEACHER_OF_PHYSYCAL_TRAINING, PHOTO_PHYSICAL_TRAINING});
        PROFECION_TEXTS.put(LIST_OF_PROFECION[1][0],new String[]{TEACHER_OF_BIO,PHOTO_BIO});
        PROFECION_TEXTS.put(LIST_OF_PROFECION[1][1],new String[]{TEACHER_OF_CHEMISTRY, PHOTO_CHEMISTRY});
        PROFECION_TEXTS.put(LIST_OF_PROFECION[1][2],new String[]{TEACHER_OF_ENGLISH,PHOTO_ENGLISH});
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            String buttonText = callbackQuery.getData();
            log.info("Im here1");
            if (Arrays.stream(LIST_OF_PROFECION[0]).toList().contains(buttonText) || Arrays.stream(LIST_OF_PROFECION[1]).toList().contains(buttonText)){
                checkTip(buttonText);
            } else if (!buttonText.equals("Загальні поради")) {
                log.info("Im here2");
                chooseProf(update);
            } else {
                showInfo();
            }

        }

        if (update.hasMessage() && update.getMessage().hasText()) {
            if (update.getMessage().getText().equals("/start")) {
                userId = update.getMessage().getChatId();
                ifClickStart(update);
            } else if (Objects.equals(update.getMessage().getText(), "Так")) {
                chooseProf(update);
            } else {
                log.warn("Unexpected update from user");
            }

        }
    }

    private void showInfo(){
        createPhoto(PHOTO_TIP,TEACHER_TIP);

        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void checkTip(String buttonText){
        createPhoto(PROFECION_TEXTS.get(buttonText)[1],PROFECION_TEXTS.get(buttonText)[0]);

        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void chooseProf(Update update){
        createButtonsOfSubjects();
        sendLog(update);
        message = SendMessage.builder()
                .chatId(userId.toString())
                .text("Виберіть професію, про яку ви хочете більше дізнатися: ")
                .build();

        message.setReplyMarkup(markup);

        createPhoto(PHOTO_TEACHING, TEACHER_SHOULD_BE);

        try {
            execute(sendPhoto);
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Exception when sending message: ", e);
        }
    }

    private void createPhoto(String filePath, String text) {
        InputFile photo = new InputFile(new File(filePath));
        sendPhoto.setChatId(String.valueOf(userId));
        sendPhoto.setCaption(text);
        sendPhoto.setPhoto(photo);
    }

    private void sendLog(Update update) {
//        String textFromUser = update.getMessage().getText();
//        String userFirstName = update.getMessage().getFrom().getFirstName();
//        String userLastName = update.getMessage().getFrom().getLastName();
//        log.info("(id = " + userId + ") " + userFirstName + " " + userLastName + ": " + textFromUser);
    }

    private void ifClickStart(Update update) {
        // Отправляем приветственное сообщение
        SendMessage message = new SendMessage();
        message.setText(START_MESSAGE);
        message.setChatId(String.valueOf(userId));
        createButtonOfSections();

        message.setReplyMarkup(markup);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void createButtonOfSections(){
        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("Загальні поради");
        button.setCallbackData(button.getText());
        row.add(button);

        InlineKeyboardButton button2 = new InlineKeyboardButton();
        button2.setText("Вибрати конкретну галузь");
        button2.setCallbackData(button2.getText());
        row.add(button2);

        List<InlineKeyboardButton> rows = new ArrayList<>();
        rows.add(button);
        rows.add(button2);

        markup.setKeyboard(Collections.singletonList(rows));
    }

    private void createButtonsOfSubjects() {
        List<InlineKeyboardButton> row = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        InlineKeyboardButton button;
        InlineKeyboardButton button2;
        for (int i = 0; i < 3; i++) {

            button = new InlineKeyboardButton();
            button.setText(LIST_OF_PROFECION[0][i]);
            button.setCallbackData(button.getText());
            row.add(button);

            button2 = new InlineKeyboardButton();
            button2.setText(LIST_OF_PROFECION[1][i]);
            button2.setCallbackData(button2.getText());
            row2.add(button2);
        }


        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        rows.add(row);
        rows.add(row2);

        markup.setKeyboard(rows);
    }

    @Override
    public String getBotUsername() {
        return "Tips for educators";
    }

    @Override
    public String getBotToken() {
        return "6268506443:AAFoK_4da-3NSrCwX5v8hT8L-KWsc8gbXw8";
    }
}
