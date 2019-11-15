package by.numberb.numberbv1.bot;

import by.numberb.numberbv1.domain.User;
import by.numberb.numberbv1.service.UserService;
import lombok.AllArgsConstructor;
import lombok.Data;
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
import java.util.regex.Pattern;

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

  public static void main(String[] args) {
    System.out.println(Math.random());
  }

  @Override
  public void onUpdateReceived(Update update) {
    test(update);
  }

  private void test(Update update) {
    if (update.hasMessage() && update.getMessage().hasText()) {
      if (userIsGame.get(update.getMessage().getFrom().getId()) == null ||
          userIsGame.get(update.getMessage().getFrom().getId()).equals(Boolean.FALSE)) {
        showMenu(update);
      }


      if (userIsGame.get(update.getMessage().getFrom().getId()).equals(Boolean.TRUE)) {
        int i = 0;
        Pattern compile = Pattern.compile("[\\d]+");
        String text = update.getMessage().getText();
        if (compile.matcher(text).matches()) {
          i = Integer.parseInt(text);
        } else {
          send(update, "Ошибка! Вы ввели буквы!");
        }
        if (gameUserTable.get(update.getMessage().getFrom().getId()).lastResult == i) {
          send(update, "Правильно");
          GameData gameData = generateData();
          UserGameData userGameData = gameUserTable.get(update.getMessage().getFrom().getId());
          userGameData.setLastResult(gameData.getResult());

          int lastResult = userGameData.getLastResult();
          userGameData.setLastResult(lastResult + 1);
          sendWithData(update, gameData.getNum1() + " " + gameData.getType() + " " + gameData.getNum2(), gameData);
        } else {
          send(update, "Не правильно");
          GameData gameData = generateData();
          gameUserTable.get(update.getMessage().getFrom().getId()).setLastResult(gameData.getResult());
          sendWithData(update, gameData.getNum1() + " " + gameData.getType() + " " + gameData.getNum2(), gameData);
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

  private void showMenu(Update update) {
    switch (update.getMessage().getText()) {
      case "/start":
        if (!userService.contains(update.getMessage().getFrom().getId())) userRegistration(update);
        send(update, "To start the game, enter /go command. If you need help, enter /help command.");
        break;
      case "/go":
        userIsGame.put(update.getMessage().getFrom().getId(), true);
        gameUserTable.put(update.getMessage().getFrom().getId(), new UserGameData());
        startGame(update);
        break;
      case "/help":
        send(update, "/go - Start Game");
        send(update, "/setting - Settings");
        send(update, "/info - Information");
        send(update, "/help - Help");
        break;
      case "/settings":
        break;
      default:
        send(update, BAD_REQUEST);
    }
  }

  private void startGame(Update update) {
//    send(update, "Level " + userService.findByChatId(update.getMessage().getChatId()).getLevel());
    GameData gameData = generateData();
    gameUserTable.get(update.getMessage().getFrom().getId()).setLastResult(gameData.getResult());
    sendWithData(update, gameData.getNum1() + " " + gameData.getType() + " " + gameData.getNum2(), gameData);
  }

  private GameData generateData() {
    double random = Math.random();
    GameData gameData = new GameData();
    int v1 = (int) (Math.random() * 10);
    int v2 = (int) (Math.random() * 10);
    gameData.setNum1(v1);
    gameData.setNum2(v2);
    if (random < 0.25) {
      gameData.setType("+");
      gameData.setResult(v1 + v2);
    } else if (random > 0.25 && random < 0.50) {
      gameData.setType("-");
      gameData.setResult(v1 - v2);
    } else if (random > 0.50 && random < 0.75) {
      gameData.setType("*");
      gameData.setResult(v1 * v2);
    } else if (random > 0.75 && random < 1) {
      gameData.setType("/");
      gameData.setResult(v1 / v2);
    }
    return gameData;
  }

  private void sendWithData(Update update, String s, GameData gameData) {
    SendMessage message = new SendMessage()
        .setChatId(update.getMessage().getChatId())
        .setText(s);
    try {
      numbersButtons(message, gameData.result, gameData.result + 15, gameData.getResult() + 7, gameData.getResult() - 6);
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

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  private static class GameData {
    int num1;
    int num2;
    String type;
    int result;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  private static class UserGameData {
    int errors;
    int lastResult;
    int rec;
  }
}
