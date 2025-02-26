package ru.netology;

import com.codeborne.selenide.Condition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;

public class RegistrationTest {
    private static final String INVALID_CITY_MESSAGE = "Доставка в выбранный город недоступна";
    private static final String INVALID_NAME_MESSAGE = "Имя и Фамилия указаны неверно. Допустимы только русские буквы, пробелы и дефисы.";
    private static final String REQUIRED_FIELD_MESSAGE = "Поле обязательно для заполнения";
    private static final String INVALID_PHONE_MESSAGE = "Телефон указан неверно. Должно быть 11 цифр, например, +79012345678.";
    private static final String INVALID_DATE_MESSAGE = "Заказ на выбранную дату невозможен";

    public static String getLocalDate(int days) {
        return LocalDate.now().plusDays(days).format(DateTimeFormatter.ofPattern("dd.MM.yyyy", new Locale("ru")));
    }

    @BeforeEach
    void openRegistrationPage() {
        open("http://localhost:9999/");
    }

    private void fillForm(String city, String date, String name, String phone) {
        $("[placeholder='Город']").val(city);
        $("[placeholder='Дата встречи']").doubleClick().sendKeys(date);
        $("[name='name']").val(name);
        $("[data-test-id='phone'] input").val(phone);
        $("[data-test-id='agreement'] .checkbox__box").click();
        $(".button__text").click();
    }

    private void checkNotification(String expectedText) {
        $("[data-test-id='notification']").should(visible, Duration.ofSeconds(15));
        $(".notification__content").shouldHave(Condition.text(expectedText), Duration.ofSeconds(15));
    }

    private void checkNoNotification() {
        $("[data-test-id='notification']").shouldNot(visible, Duration.ofSeconds(15));
    }

    @Test
    void shouldTestNotValidCity() {
        String planningDate = getLocalDate(5);
        fillForm("Минск", planningDate, "Иванов Иван", "+79998887766");
        $("[data-test-id='city'].input_invalid .input__sub")
            .shouldHave(exactText(INVALID_CITY_MESSAGE));
        checkNoNotification();
    }

    @Test
    void shouldTestCityEnglish() {
        String planningDate = getLocalDate(6);
        fillForm("Moscow", planningDate, "Иван Иванов", "+79998887766");
        $("[data-test-id='city'].input_invalid .input__sub")
            .shouldHave(exactText(INVALID_CITY_MESSAGE));
        checkNoNotification();
    }

    @Test
    void shouldTestCityEmpty() {
        String planningDate = getLocalDate(6);
        fillForm("", planningDate, "Иван Иванов", "+79998887766");
        $("[data-test-id='city'].input_invalid .input__sub")
            .shouldHave(exactText(REQUIRED_FIELD_MESSAGE));
        checkNoNotification();
    }

    @Test
    void shouldTestDoubleFirstName() {
        String planningDate = getLocalDate(6);
        fillForm("Санкт-Петербург", planningDate, "Иван Иванов-Петров", "+79998887766");
        checkNotification("Встреча успешно забронирована на " + planningDate);
    }

    @Test
    void shouldTestNameWithNum() {
        String planningDate = getLocalDate(6);
        fillForm("Санкт-Петербург", planningDate, "14567 1564", "+79998887766");
        $("[data-test-id='name'].input_invalid .input__sub")
            .shouldHave(exactText(INVALID_NAME_MESSAGE));
        checkNoNotification();
    }

    @Test
    void shouldTestNameEnglish() {
        String planningDate = getLocalDate(6);
        fillForm("Санкт-Петербург", planningDate, "Ivan Ivanov", "+79998887766");
        $("[data-test-id='name'].input_invalid .input__sub")
            .shouldHave(exactText(INVALID_NAME_MESSAGE));
        checkNoNotification();
    }

    @Test
    void shouldTestNameWithSpecSymbols() {
        String planningDate = getLocalDate(6);
        fillForm("Санкт-Петербург", planningDate, "Иван Иванов!", "+79998887766");
        $("[data-test-id='name'].input_invalid .input__sub")
            .shouldHave(exactText(INVALID_NAME_MESSAGE));
        checkNoNotification();
    }

    @Test
    void shouldTestPhoneWithoutPlus() {
        String planningDate = getLocalDate(90);
        fillForm("Санкт-Петербург", planningDate, "Иван Иванов", "89998887766");
        $("[data-test-id='phone'].input_invalid .input__sub")
            .shouldHave(exactText(INVALID_PHONE_MESSAGE));
        checkNoNotification();
    }

    @Test
    void shouldTestPhoneWithOneNumber() {
        String planningDate = getLocalDate(90);
        fillForm("Санкт-Петербург", planningDate, "Иван Иванов", "+7999888776");
        $("[data-test-id='phone'].input_invalid .input__sub")
            .shouldHave(exactText(INVALID_PHONE_MESSAGE));
        checkNoNotification();
    }

    @Test
    void shouldTestPhoneWithSpecSymbols() {
        String planningDate = getLocalDate(90);
        fillForm("Санкт-Петербург", planningDate, "Иван Иванов", "+7(999)-888-77 66");
        $("[data-test-id='phone'].input_invalid .input__sub")
            .shouldHave(exactText(INVALID_PHONE_MESSAGE));
        checkNoNotification();
    }

    @Test
    void shouldTestPhoneEmpty() {
        String planningDate = getLocalDate(90);
        fillForm("Санкт-Петербург", planningDate, "Иван Иванов", "");
        $("[data-test-id='phone'].input_invalid .input__sub")
            .shouldHave(exactText(REQUIRED_FIELD_MESSAGE));
        checkNoNotification();
    }

    @Test
    void shouldTestNextDayMeeting() {
        String planningDate = getLocalDate(1);
        fillForm("Санкт-Петербург", planningDate, "Иван Иванов", "+79998887766");
        $("[data-test-id='date'] .input_invalid .input__sub")
            .shouldHave(exactText(INVALID_DATE_MESSAGE));
        checkNoNotification();
    }

    @Test
    void shouldTestPlus0days() {
        String planningDate = getLocalDate(0);
        fillForm("Санкт-Петербург", planningDate, "Иван Иванов", "+79998887766");
        $("[data-test-id='date'] .input_invalid .input__sub")
            .shouldHave(exactText(INVALID_DATE_MESSAGE));
        checkNoNotification();
    }

    @Test
    void shouldTestMinus5Days() {
        String planningDate = getLocalDate(-5);
        fillForm("Санкт-Петербург", planningDate, "Иван Иванов", "+79998887766");
        $("[data-test-id='date'] .input_invalid .input__sub")
            .shouldHave(exactText(INVALID_DATE_MESSAGE));
        checkNoNotification();
    }

    @Test
    void shouldTestFebruaryDays() {
        fillForm("Санкт-Петербург", "30.02.2023", "Иван Иванов", "+79998887766");
        $("[data-test-id='date'] .input_invalid .input__sub")
            .shouldHave(exactText("Неверно введена дата"));
        checkNoNotification();
    }

    @Test
    void shouldTestUncheckedBox() {
        String planningDate = getLocalDate(4);
        fillForm("Санкт-Петербург", planningDate, "Иван Иванов", "+79998887766");
        $("[data-test-id='agreement'].input_invalid").should(exist);
        checkNoNotification();
    }

    @Test
    void shouldTestCheckedCheckedBox() {
        String planningDate = getLocalDate(4);
        fillForm("Санкт-Петербург", planningDate, "Иван Иванов", "+79998887766");
        checkNotification("Встреча успешно забронирована на " + planningDate);
    }
}