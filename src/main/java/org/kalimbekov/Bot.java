package org.kalimbekov;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Bot extends TelegramLongPollingBot {
    ArrayList<State> states = new ArrayList<>();
    ReplyKeyboardMarkup mainMenuReplyKeyboardMarkup;

    public Bot() {
        this.mainMenuReplyKeyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();
        for (int i = 1; i != 8; i++) {
            row.add(new KeyboardButton(Integer.toString(i)));
        }
        keyboard.add(row);

        mainMenuReplyKeyboardMarkup.setResizeKeyboard(true);
        mainMenuReplyKeyboardMarkup.setOneTimeKeyboard(true);
        mainMenuReplyKeyboardMarkup.setKeyboard(keyboard);
    }

    @Override
    public String getBotUsername() {
        return "kalimbekov_math_bot";
    }

    @Override
    public String getBotToken() {
        try (Scanner scanner = new Scanner(new File("data/token"))) {
            String token = scanner.nextLine();
            if (token.isBlank()) {
                System.err.println("""
                        "token" file under "data" directory is empty. Please
                        put your Telegram bot token inside.
                        """);
                throw new RuntimeException("""
                        "token" file is blank
                        """);
            }
            return token;
        } catch (FileNotFoundException e) {
            System.err.println("""
                    No "token" file found at under "data" directory. Create one
                    and put your Telegram bot token inside.
                    """);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        String messageText = message.getText();
        User user = message.getFrom();

        State state = null;
        boolean newUser = true;
        for (State currentState : this.states) {
            if (currentState.getUser().equals(user)) {
                state = currentState;
                newUser = false;
                break;
            }
        }
        if (newUser) {
            state = new State(user);
            this.states.add(state);
        }

        if (message.isCommand()) {
            if (messageText.equalsIgnoreCase("/start")) {
                sendImage(user.getId(), "assets/images/variants.png", "");
                sendMenu(user.getId(), "Pick a variant", this.mainMenuReplyKeyboardMarkup);
                state.setVariant(0);
            } else {
                sendText(user.getId(), """
                        "/start" is the only available command
                        """);
            }
        } else if (state.getVariant() == 0) { // User has to pick the variant
            try {
                int variant = Integer.parseInt(messageText);
                if (variant > 0 && variant < 8) { // Valid variant number
                    state.setVariant(variant);
                    sendImage(user.getId(), "assets/images/" + state.getVariant() + ".png",
                            "Enter real numbers for " + state.getNeededVariables() +
                                    " accordingly, separated by spaces");
                } else {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException e) {
                sendMenu(user.getId(), "Pick a variant", this.mainMenuReplyKeyboardMarkup);
            }
        } else { // Now the user has to enter numbers
            try {
                String[] numberStrings = messageText.split(" ");
                if (numberStrings.length != state.getLength()) {
                    throw new NumberFormatException();
                } else {
                    double[] numbers = new double[numberStrings.length];
                    for (int i = 0; i < numberStrings.length; i++) {
                        numbers[i] = Double.parseDouble(numberStrings[i]);
                    }

                    double result = solve(state.getVariant(), numbers);
                    if (Double.isInfinite(result)) {
                        sendText(user.getId(), String.format("Variant: %d\nArguments: %s\nResult: undefined",
                                state.getVariant(), String.join(", ", numberStrings)));
                    } else {
                        sendText(user.getId(), String.format("Variant: %d\nArguments: %s\nResult: %f",
                                state.getVariant(), String.join(", ", numberStrings), result));
                    }

                    state.setVariant(0);
                    sendMenu(user.getId(), "Pick a variant", this.mainMenuReplyKeyboardMarkup);
                }
            } catch (NumberFormatException e) {
                sendText(user.getId(), "Enter " + state.getLength() + " real numbers " +
                        "separated by spaces");
            }
        }
    }

    public void sendText(Long who, String what){
        SendMessage sm = SendMessage.builder()
                .chatId(who.toString())
                .text(what).build();
        try {
            execute(sm);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendMenu(Long who, String txt, ReplyKeyboardMarkup kb){
        SendMessage sm = SendMessage.builder()
                .chatId(who.toString())
                .text(txt)
                .replyMarkup(kb)
                .build();
        try {
            execute(sm);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendImage(Long who, String imagePath, String caption) {
        SendPhoto sendPhoto = SendPhoto.builder()
                .chatId(who.toString())
                .photo(new InputFile(new File(imagePath)))
                .caption(caption)
                .build();
        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private double solve(int variant, double[] args) {
        return switch (variant) {
            case 1 -> {
                double a = args[0];
                double b = args[1];
                double c = args[2];
                double n = args[3];
                double x = args[4];

                yield (5 * Math.pow(a, n * x)) / (b + c) - Math.sqrt(Math.abs(Math.cos(x * x * x)));
            }
            case 2 -> {
                double a = args[0];
                double o = args[1];
                double x = args[2];
                double y = args[3];

                yield (Math.abs(x - y)) / Math.pow(1 + 2 * x, a) - Math.pow(Math.E, Math.sqrt(1 + o));
            }
            case 3 -> {
                double a0 = args[0];
                double a1 = args[1];
                double a2 = args[2];
                double x = args[3];

                yield Math.sqrt( a0 + a1 * x + a2 * Math.pow(Math.abs(Math.sin(x)), 1.0/3.0) );
            } case 4 -> {
                double a = args[0];
                double x = args[1];

                yield Math.log(Math.abs(Math.pow(a, 7))) + Math.atan(x * x) + Math.PI / Math.sqrt(Math.abs(a + x));
            }
            case 5 -> {
                double a = args[0];
                double b = args[1];
                double c = args[2];
                double d = args[3];
                double x = args[4];

                yield Math.pow( Math.pow(a + b, 2) / (c + d) + Math.pow(Math.E, Math.sqrt(x + 1)), 0.2 );
            }
            case 6 -> {
                double x = args[0];

                yield Math.pow( Math.E, (2 * Math.sin(4 * x) + Math.pow(Math.cos(x * x), 2)) / 3 * x );
            } case 7 -> {
                double x = args[0];

                yield 0.25 * ( (1 + x * x) / (1 - x) + 0.25 * Math.tan(x) );
            }
            default -> throw new IllegalStateException("Unexpected value: " + variant);
        };
    }
}
