package com.example.value_test;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private EditText urlEditText;
    private Button downloadButton;
    private TextView downloadStatus;
    private String downloadUrl; // Для хранения ссылки на скачивание
    private Button viewButton;
    private Button deleteButton;
    private ProgressBar loadingProgressBar;
    private File downloadedFile; // Переменная для хранения

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE},
                PackageManager.PERMISSION_GRANTED);


        urlEditText = findViewById(R.id.urlEditText);
        downloadButton = findViewById(R.id.downloadButton);
        downloadStatus = findViewById(R.id.downloadStatus);
//        viewButton = findViewById(R.id.viewButton);
//        deleteButton = findViewById(R.id.deleteButton);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);


        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                downloadUrl = urlEditText.getText().toString();
                downloadJournal();
            }
        });
//
//        viewButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                // Обработка нажатия на кнопку "Смотреть"
//                viewJournal();
//            }
//        });

//        deleteButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                // Обработка нажатия на кнопку "Удалить"
//                deleteJournal();
//            }
//        });
    }
    private void downloadJournal() {
        OkHttpClient client = new OkHttpClient();
        loadingProgressBar.setVisibility(View.VISIBLE);

        // Check if the URL has a scheme (http or https), and add one if not
        if (!downloadUrl.startsWith("http://") && !downloadUrl.startsWith("https://")) {
            downloadUrl = "https://" + downloadUrl;  // You can choose http:// or https:// based on your requirements
        }

        Request request = new Request.Builder()
                .url(downloadUrl)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        downloadStatus.setText("Ошибка при скачивании.");
                        loadingProgressBar.setVisibility(View.GONE);
                        Log.d("ViewJournal", "Путь к файлу: " + e.getMessage());
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            downloadStatus.setText("Не удалось загрузить файл.");
                            loadingProgressBar.setVisibility(View.GONE);
                        }
                    });
                } else {
                    final File journalFolder = getDownloadDirectory(MainActivity.this);

                    if (!journalFolder.exists()) {
                        journalFolder.mkdirs();
                    }

                    final File journalFile = new File(journalFolder, "journal.pdf");

                    try {
                        // Before creating a new file, delete the existing file if it exists
                        if (journalFile.exists()) {
                            journalFile.delete();
                        }

                        FileOutputStream outputStream = new FileOutputStream(journalFile);
                        outputStream.write(response.body().bytes());
                        outputStream.close();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                downloadStatus.setText("Файл успешно скачан.");
                                Toast.makeText(MainActivity.this, "Файл сохранен в " + journalFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
                                downloadedFile = journalFile;
                                loadingProgressBar.setVisibility(View.GONE);
                                showSuccessDialog(); // Show the success dialog
                            }
                        });
                    } catch (IOException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                downloadStatus.setText("Ошибка при сохранении файла." + e.getMessage());
                                loadingProgressBar.setVisibility(View.GONE);
                            }
                        });
                    }
                }
            }
        });
    }

//    private void downloadJournal() {
//        OkHttpClient client = new OkHttpClient();
//        loadingProgressBar.setVisibility(View.VISIBLE);
//
//        // Check if the URL has a scheme (http or https), and add one if not
//        if (!downloadUrl.startsWith("http://") && !downloadUrl.startsWith("https://")) {
//            downloadUrl = "https://" + downloadUrl;  // You can choose http:// or https:// based on your requirements
//        }
//
//        Request request = new Request.Builder()
//                .url(downloadUrl)
//                .build();
//
//        client.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        downloadStatus.setText("Ошибка при скачивании.");
//                        loadingProgressBar.setVisibility(View.GONE);
//                        Log.d("ViewJournal", "Путь к файлу: " + e.getMessage());
//                    }
//                });
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                if (!response.isSuccessful()) {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            downloadStatus.setText("Не удалось загрузить файл.");
//                            loadingProgressBar.setVisibility(View.GONE);
//                        }
//                    });
//                } else {
//                    final File journalFolder = getDownloadDirectory(MainActivity.this);
//
//                    if (!journalFolder.exists()) {
//                        journalFolder.mkdirs();
//                    }
//
//                    final File journalFile = new File(journalFolder, "journal.pdf");
//
//                    try {
//                        // Before creating a new file, delete the existing file if it exists
//                        if (journalFile.exists()) {
//                            journalFile.delete();
//                        }
//
//                        FileOutputStream outputStream = new FileOutputStream(journalFile);
//                        outputStream.write(response.body().bytes());
//                        outputStream.close();
//
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                downloadStatus.setText("Файл успешно скачан.");
//                                Toast.makeText(MainActivity.this, "Файл сохранен в " + journalFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
//                                downloadedFile = journalFile;
//                                loadingProgressBar.setVisibility(View.GONE);
//                            }
//                        });
//                    } catch (IOException e) {
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                downloadStatus.setText("Ошибка при сохранении файла." + e.getMessage());
//                                loadingProgressBar.setVisibility(View.GONE);
//                            }
//                        });
//                    }
//                }
//            }
//        });
//    }

    private void viewJournal() {
        if (downloadedFile != null && downloadedFile.exists()) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri fileUri = FileProvider.getUriForFile(this, "com.example.value_test.provider", downloadedFile);

//            Uri fileUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", downloadedFile);
            intent.setDataAndType(fileUri, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try {
                this.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(MainActivity.this, "Для просмотра PDF не установлено подходящее приложение.", Toast.LENGTH_SHORT).show();
                // If Google Play is not installed, offer an alternative installation method
                Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.adobe.reader"));
                this.startActivity(webIntent);
            }
        } else {
            Toast.makeText(MainActivity.this, "Файл не найден. Сначала скачайте его.", Toast.LENGTH_SHORT).show();
        }
    }

//    private void viewJournal() {
//        if (downloadedFile != null && downloadedFile.exists()) {
//
//            Intent intent = null;
//            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
//                intent = new Intent(Intent.ACTION_VIEW, MediaStore.Downloads.EXTERNAL_CONTENT_URI);
//            }
//            intent.setType("application/pdf");
//
////            Intent viewPDFIntent = new Intent(Intent.ACTION_VIEW);
////            viewPDFIntent.setDataAndType(Uri.fromFile(downloadedFile),"application/pdf" );
////            viewPDFIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
////            this.startActivity(Intent.createChooser(viewPDFIntent, "test"));
//            try {
//                this.startActivity(intent);
//                } catch (ActivityNotFoundException e) {
//                    Toast.makeText(MainActivity.this, "Для просмотра PDF не установлено подходящее приложение.", Toast.LENGTH_SHORT).show();
//                    // Если Google Play не установлен, предложите альтернативный способ установки
//                    Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.adobe.reader"));
//                    this.startActivity(webIntent);
//                }
//        }
//        else {
//            Toast.makeText(MainActivity.this, "Файл не найден. Сначала скачайте его.", Toast.LENGTH_SHORT).show();
//        }
//    }

    private void showSuccessDialog() {
        View dialogView = View.inflate(this, R.layout.dialog_layout, null);
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this)
                .setView(dialogView);

        final AlertDialog alertDialog = dialogBuilder.create();

        Button viewButtonInDialog = dialogView.findViewById(R.id.viewButtonInDialog);
        Button deleteButtonInDialog = dialogView.findViewById(R.id.deleteButtonInDialog);

        viewButtonInDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss(); // Close the dialog
                viewJournal();
            }
        });

        deleteButtonInDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss(); // Close the dialog
                deleteJournal();
            }
        });

        alertDialog.show();
    }

    private void deleteJournal() {
        // Обработка нажатия на кнопку "Удалить"
        if (downloadedFile != null && downloadedFile.exists()) {
            if (downloadedFile.delete()) {
                downloadStatus.setText("Файл удален.");
                Toast.makeText(MainActivity.this, "Файл успешно удален.", Toast.LENGTH_SHORT).show();
                downloadedFile = null; // Сброс переменной downloadedFile
            } else {
                Toast.makeText(MainActivity.this, "Ошибка при удалении файла.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(MainActivity.this, "Файл не найден. Сначала скачайте его.", Toast.LENGTH_SHORT).show();
        }
    }

    public File getDownloadDirectory(Context context) {
        File downloadDir;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            downloadDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Naychyj vestnik");
        } else {
            downloadDir = new File(context.getFilesDir(), "Naychyj vestnik");
        }
        if (!downloadDir.exists()) {
            downloadDir.mkdirs();
        }
        return downloadDir;
    }
}