package hindi.english.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    EditText et_1;
    TextView txt, txt_lan_1, txt_lan_2;
    Translator englishHindiTranslator, hindiEnglishTranslator;
    ClipboardManager clipboard;
    ClipData clip;
    Boolean flag = true;
    Dialog dialog;
    TranslatorOptions options_2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        et_1 = findViewById(R.id.et_1);
        txt = findViewById(R.id.txt);
        txt_lan_1 = findViewById(R.id.txt_lan_1);
        txt_lan_2 = findViewById(R.id.txt_lan_2);
        findViewById(R.id.swap).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swap();
            }
        });
        findViewById(R.id.mic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                voice();
            }
        });
        findViewById(R.id.cp_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    copy(et_1.getText().toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        findViewById(R.id.cp_2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    copy(txt.getText().toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

        dialog = new Dialog(MainActivity.this, android.R.style.Theme_Dialog);
        open_dialog();
        et_1.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                // TODO Auto-generated method stub
                if (flag)
                    translate_hin(et_1.getText().toString());
                else translate_eng(et_1.getText().toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable s) {

                // TODO Auto-generated method stub
            }
        });

        // Create an English-Hindi translator:
        TranslatorOptions options =
                new TranslatorOptions.Builder()
                        .setSourceLanguage(TranslateLanguage.ENGLISH)
                        .setTargetLanguage(TranslateLanguage.HINDI)
                        .build();

        options_2 =
                new TranslatorOptions.Builder()
                        .setSourceLanguage(TranslateLanguage.HINDI)
                        .setTargetLanguage(TranslateLanguage.ENGLISH)
                        .build();

        englishHindiTranslator =
                Translation.getClient(options);

        DownloadConditions conditions = new DownloadConditions.Builder()
                .build();
        englishHindiTranslator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void v) {
                                // Model downloaded successfully. Okay to start translating.
                                download_hin_eng();
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Model couldn’t be downloaded or other internal error.
                                txt.setText(e.getMessage());
                            }
                        });
    }

    void voice() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        if (flag)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.ENGLISH);
        else intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi");
        try {
            startActivityForResult(intent, 200);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(), "Intent Problem", Toast.LENGTH_LONG).show();
        }
    }

    public void open_dialog() {
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setContentView(R.layout.dialog_loading);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        dialog.setCancelable(false);
        dialog.show();
    }

    void swap() {
        String a = txt_lan_1.getText().toString();
        String b = txt_lan_2.getText().toString();
        a = a + b;
        b = a.substring(0, a.length() - b.length());
        a = a.substring(b.length());
        txt_lan_1.setText(a);
        txt_lan_2.setText(b);
        if (flag)
            flag = false;
        else flag = true;
        et_1.setText(null);
        txt.setText(null);


    }

    void download_hin_eng() {
        hindiEnglishTranslator = Translation.getClient(options_2);
        hindiEnglishTranslator.downloadModelIfNeeded()
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void v) {
                                // Model downloaded successfully. Okay to start translating.
                                // (Set a flag, unhide the translation UI, etc.)
//                                findViewById(R.id.btn).setEnabled(true);
//                                Toast.makeText(getApplicationContext(),"Second done",Toast.LENGTH_SHORT).show();
                                txt.setText(null);
                                dialog.dismiss();
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Model couldn’t be downloaded or other internal error.
                                // ...
                                txt.setText(e.getMessage());
                            }
                        });
    }

    void copy(String text) {
        if (!text.equals("")) {
            clip = ClipData.newPlainText("text", text);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(getApplicationContext(), "Text Copied", Toast.LENGTH_LONG).show();

        } else {
            Toast.makeText(getApplicationContext(), "Text Copied", Toast.LENGTH_LONG).show();
//            mdToast = MDToast.makeText(getApplicationContext(), "There is no text", MDToast.LENGTH_SHORT, MDToast.TYPE_WARNING);
        }
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 200) {
            if (resultCode == RESULT_OK && data != null) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                assert result != null;
                et_1.setText(result.get(0));

                if (flag)
                    translate_hin(et_1.getText().toString().trim());
                else translate_eng(et_1.getText().toString().trim());

            }
        }
    }

    void translate_hin(String text) {
        englishHindiTranslator.translate(text)
                .addOnSuccessListener(
                        new OnSuccessListener<String>() {
                            @Override
                            public void onSuccess(@NonNull String translatedText) {
                                // Translation successful.
                                txt.setText(translatedText);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Error.
                                // ...
                                txt.setText(e.getMessage());
                            }
                        });
    }

    void translate_eng(String text) {
        hindiEnglishTranslator.translate(text)
                .addOnSuccessListener(
                        new OnSuccessListener<String>() {
                            @Override
                            public void onSuccess(@NonNull String translatedText) {
                                // Translation successful.
                                txt.setText(translatedText);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Error.
                                // ...
                                txt.setText(e.getMessage());
                            }
                        });
    }
}