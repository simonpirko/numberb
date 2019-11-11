package by.numberb.numberbv1;

import by.numberb.numberbv1.domain.User;
import by.numberb.numberbv1.service.UserService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class Bot extends TelegramLongPollingBot {

  private final static String BAD_REQUEST = "Oops!";
  private final UserService userService;
  @Value("${bot.name}")
  private String botName;
  @Value("${bot.token}")
  private String token;

  private Map<Integer, Boolean> userIsGame = new ConcurrentHashMap<>();
  private Map<Integer, UserGameData> gameUserTable = new ConcurrentHashMap<>();


  public Bot(UserService userService) {
    this.userService = userService;
  }

  @Override
  public void onUpdateReceived(Update update) {
    test(update);
  }


  private void test(Update update) {
    if (update.hasMessage() && update.getMessage().hasText()) {
      if (userIsGame.get(update.getMessage().getFrom().getId()) == null || userIsGame.get(update.getMessage().getFrom().getId()).equals(Boolean.FALSE)) {
        switch (update.getMessage().getText()) {
          case "users":
            send(update, userService.findAll().toString());
            break;
          case "/start":
            if (!userService.contains(update.getMessage().getFrom().getId())) userRegistration(update);
            send(update, "/help - Help");
            send(update, "/go - Start Game");
            send(update, "/setting - Settings");
            send(update, "/info - Information");
            break;
          case "/go":
            userIsGame.put(update.getMessage().getFrom().getId(), true);
            gameUserTable.put(update.getMessage().getFrom().getId(), new UserGameData());
            startGame(update);
            break;
          case "/help":
            send(update, "/help - Help");
            send(update, "/go - Start Game");
            send(update, "/setting - Settings");
            send(update, "/info - Information");
            break;
          case "/settings":

            break;
          default:
            send(update, BAD_REQUEST);
        }
      }
      if (userIsGame.get(update.getMessage().getFrom().getId()).equals(Boolean.TRUE)) {
        int i = Integer.parseInt(update.getMessage().getText());
        if (gameUserTable.get(update.getMessage().getFrom().getId()).lastResult == i) {
          Data data = generateData();
          send(update, "Правильно");
          UserGameData userGameData = gameUserTable.get(update.getMessage().getFrom().getId());
          userGameData.setLastResult(data.getResult());
          int lastResult = userGameData.getLastResult();
          userGameData.setLastResult(lastResult + 1);
          sendWithData(update, data.getNum1() + " " + data.getType() + " " + data.getNum2(), data);
        } else {
          Data data = generateData();
          send(update, "Не правильно");
          gameUserTable.get(update.getMessage().getFrom().getId()).setLastResult(data.getResult());
          sendWithData(update, data.getNum1() + " " + data.getType() + " " + data.getNum2(), data);
          int errors = gameUserTable.get(update.getMessage().getFrom().getId()).getErrors();
          if (errors > 2) {
            sendEndGame(update, "Игра окончена!");
            userIsGame.put(update.getMessage().getFrom().getId(), Boolean.FALSE);
            userService.updateScore(update.getMessage().getChatId(), gameUserTable.get(update.getMessage().getFrom().getId()).getLastResult());
          }
          gameUserTable.get(update.getMessage().getFrom().getId()).setErrors(errors + 1);
        }
      }
    }
  }

  private void startGame(Update update) {
    send(update, "Level " + userService.findByChatId(update.getMessage().getChatId()).getLevel());
    Data data = generateData();
    gameUserTable.get(update.getMessage().getFrom().getId()).setLastResult(data.getResult());
    sendWithData(update, data.getNum1() + " " + data.getType() + " " + data.getNum2(), data);
  }

  private Data generateData() {
    Data data = new Data();
    int v1 = (int) (Math.random() * 10);
    int v2 = (int) (Math.random() * 10);
    data.setNum1(v1);
    data.setNum2(v2);
    data.setType("+");
    data.setResult(v1 + v2);
    return data;
  }

  private void sendWithData(Update update, String s, Data data) {
    SendMessage message = new SendMessage()
        .setChatId(update.getMessage().getChatId())
        .setText(s);
    try {
      numbersButtons(message, data.result, data.result + 15, data.getResult() + 7, data.getResult() - 6);
      execute(message);
    } catch (TelegramApiException e) {
      e.printStackTrace();
    }
  }

  private void send(Update update, String s) {
    SendMessage message = new SendMessage()
        .setChatId(update.getMessage().getChatId())
        .setText(s);
    try {
      execute(message);
    } catch (TelegramApiException e) {
      e.printStackTrace();
    }
  }

  private void sendEndGame(Update update, String s) {
    SendMessage message = new SendMessage()
        .setChatId(update.getMessage().getChatId())
        .setText(s);
    try {
      endButtons(message);
      execute(message);
    } catch (TelegramApiException e) {
      e.printStackTrace();
    }
  }

  private void numbersButtons(SendMessage sendMessage, int... numbers) {
    ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
    sendMessage.setReplyMarkup(replyKeyboardMarkup);
    replyKeyboardMarkup.setSelective(true);
    replyKeyboardMarkup.setResizeKeyboard(true);
    replyKeyboardMarkup.setOneTimeKeyboard(false);

    List<KeyboardRow> keyboardRowList = new ArrayList<>();
    KeyboardRow keyboardFirstRow = new KeyboardRow();

    keyboardFirstRow.add(new KeyboardButton(String.valueOf(numbers[0])));
    keyboardFirstRow.add(new KeyboardButton(String.valueOf(numbers[1])));
    keyboardFirstRow.add(new KeyboardButton(String.valueOf(numbers[2])));
    keyboardFirstRow.add(new KeyboardButton(String.valueOf(numbers[3])));

    keyboardRowList.add(keyboardFirstRow);
    replyKeyboardMarkup.setKeyboard(keyboardRowList);

  }

  private void endButtons(SendMessage sendMessage) {
//    ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
//    sendMessage.setReplyMarkup(replyKeyboardMarkup);
//    replyKeyboardMarkup.setSelective(true);
//    replyKeyboardMarkup.setResizeKeyboard(true);
//    replyKeyboardMarkup.setOneTimeKeyboard(false);
    sendMessage.setReplyMarkup(null);

//    List<KeyboardRow> keyboardRowList = new ArrayList<>();
//    KeyboardRow keyboardFirstRow = new KeyboardRow();
//
//    keyboardRowList.add(keyboardFirstRow);
//    replyKeyboardMarkup.setKeyboard(keyboardRowList);
  }

  private void userRegistration(Update update) {
    User user = new User();
    user.setClientId(update.getMessage().getFrom().getId());
    user.setChatId(update.getMessage().getChatId());
    user.setFirstName(update.getMessage().getFrom().getFirstName());
    user.setLastName(update.getMessage().getFrom().getLastName());
    user.setLogin(update.getMessage().getFrom().getUserName());
    userService.createUser(user);
  }

  @Override
  public String getBotUsername() {
    return botName;
  }

  @Override
  public String getBotToken() {
    return token;
  }

  @lombok.Data
  @NoArgsConstructor
  @AllArgsConstructor
  private class Data {
    int num1;
    int num2;
    String type;
    int result;
  }

  private class UserGameData {
    int errors;
    int lastResult;
    int rec;

    public int getRec() {
      return rec;
    }

    public void setRec(int rec) {
      this.rec = rec;
    }

    public int getErrors() {
      return errors;
    }

    public void setErrors(int errors) {
      this.errors = errors;
    }

    public int getLastResult() {
      return lastResult;
    }

    public void setLastResult(int lastResult) {
      this.lastResult = lastResult;
    }
  }
}
