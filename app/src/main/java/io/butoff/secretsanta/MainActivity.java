package io.butoff.secretsanta;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Timer;
import java.util.TimerTask;

import ru.evotor.devices.commons.DeviceServiceConnector;
import ru.evotor.devices.commons.exception.DeviceServiceException;
import ru.evotor.devices.commons.exception.ServiceNotConnectedException;
import ru.evotor.devices.commons.printer.PrinterDocument;
import ru.evotor.devices.commons.printer.printable.PrintableText;
import ru.evotor.devices.commons.services.IPrinterServiceWrapper;

import static ru.evotor.devices.commons.Constants.DEFAULT_DEVICE_INDEX;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    Gift[] gifts;
    String userInput = "mr.unknown";

    Gson gson;

    EditText loginEditText;

    AsyncTask<Gift, Void, String> printTask;
    private AlertDialog finalAlert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        initViews();
        readData();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (printTask != null)
            printTask.cancel(true);
    }

    private void initViews() {
        initInput();
    }

    private void onPrintClick() {
        printOrError();
    }

    private void printOrError() {
        userInput = loginEditText.getText().toString();
        Gift gift = getNameOrError();
        if (gift != null)
            printOrErrorMessage(gift);
    }

    private void initInput() {
        loginEditText = findViewById(R.id.login);
        loginEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == 6)
                    onPrintClick();
                return false;
            }
        });
    }

    @SuppressLint("StaticFieldLeak")
    private void printOrErrorMessage(Gift gift) {
        if (printTask != null) {
            printTask.cancel(true);
        }
        printTask = new AsyncTask<Gift, Void, String>() {
            @Override
            protected String doInBackground(Gift... gift) {
                try {
                    print(gift[0]);
                } catch (ServiceNotConnectedException e) {
                    Log.e(TAG, "doInBackground: ", e);
                    return "Ошибка при подключении: " + e.getMessage();
                } catch (DeviceServiceException e) {
                    Log.e(TAG, "doInBackground: ", e);
                    return "Ошибка при печати: " + e.getMessage();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String errorMessage) {
                if (errorMessage != null)
                    errorToast(MainActivity.this, errorMessage);
                else {
                    finalAlert = alert("Санта подобрал тебе жертву", "Не забудь взять чек!");
                    loginEditText.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            loginEditText.setText("");
                        }
                    }, 2 * 1000);
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            finalAlert.dismiss();
                        }
                    }, 8 * 1000);
                }
            }
        }.execute(gift);
    }

    private void print(Gift gift) throws DeviceServiceException {
        // TODO: 07.12.18 должен же быть IPrinterService ?
        IPrinterServiceWrapper printerService = DeviceServiceConnector.getPrinterService();
        PrinterDocument printerDocument = prepareDocument(gift);
        printerService.printDocument(DEFAULT_DEVICE_INDEX, printerDocument);
    }

    private AlertDialog alert(String title, String message) {
        return new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        loginEditText.setText("");
                    }
                })
                .show();
    }

    private void errorToast(Context context, String errorMessage) {
        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show();
    }

    private PrinterDocument prepareDocument(Gift gift) {
        String header = prepareHeader(gift);
        String footer = "ХО-ХО-ХО!!!";
        return new PrinterDocument(
                new PrintableText("ПРИВЕТ ОТ ЭВОСАНТЫ"),
                new PrintableText(" "),
                new PrintableText(header),
                new PrintableText(" "),
                new PrintableText(gift.consumerName),
                new PrintableText(gift.consumerEmail),
                new PrintableText(" "),
                new PrintableText(footer)
//             ,new PrintableImage(bitmap1)
        );
    }

    @NonNull
    private String prepareHeader(Gift gift) {
        return gift.providerName + ", в этот новый год подарок от тебя ждет...\n\n";
    }

    private Gift getNameOrError() {
        try {
            return getGift();
        } catch (NotFoundConsumer e) {
            alert("Санта никого для тебя не нашел :(", "Расскажи об этом моим помощникам");
        } catch (NotFoundProducer e) {
            alert("Санта тебя не нашел :(", userInput + ", мы точно знакомы?");
        } catch (EmptyInputException e) {
            alert("Санта тебя не узнал :(", "Нужно ввести свой логин");
        } catch (SantaException e) {
            alert("Санта промахнулся :(", "Похоже, вам нужен новый санта");
        }
        return null;
    }

    private Gift getGift() throws SantaException {
        if (userInput.isEmpty())
            throw new EmptyInputException();
        Gift gift = getGiftByEmail(userInput);
        if (gift == null) {
            String myEmail = LoginUtils.getEmailByLogin(userInput);
            gift = getGiftByEmail(myEmail);
        }
        if (gift == null) {
            gift = getGiftByName(userInput);
        }
        if (gift == null)
            throw new NotFoundProducer();
        return gift;
    }

    @Nullable
    private Gift getGiftByEmail(String myEmail) throws NotFoundConsumer {
        for (Gift gift : gifts) {
            if (gift.providerEmail.equals(myEmail)) {
                if (gift.consumerEmail.isEmpty())
                    throw new NotFoundConsumer();
                return gift;
            }
        }
        return null;
    }

    @Nullable
    private Gift getGiftByName(String myName) throws NotFoundConsumer {
        for (Gift gift : gifts) {
            if (LoginUtils.equalsIgnoreWhitespaces(gift.providerName, myName)) {
                if (gift.consumerEmail.isEmpty())
                    throw new NotFoundConsumer();
                return gift;
            }
        }
        return null;
    }

    private void readData() {
        gson = new Gson();
        InputStream is = getResources().openRawResource(R.raw.some);
        Reader reader = new BufferedReader(new InputStreamReader(is));
        gifts = gson.fromJson(reader, Gift[].class);
    }

    public static class SantaException extends Exception {
    }

    public static class NotFoundProducer extends SantaException {
    }

    public static class NotFoundConsumer extends SantaException {
    }

    public static class EmptyInputException extends SantaException {
    }
}
