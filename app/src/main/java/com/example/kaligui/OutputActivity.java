package com.example.kaligui;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class OutputActivity extends AppCompatActivity implements SshManager.CommandListener {

    private TextView outputTextView;
    private ProgressBar progressBar;
    private SshManager sshManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_output);

        outputTextView = findViewById(R.id.output_textview);
        progressBar = findViewById(R.id.output_progress_bar);
        outputTextView.setMovementMethod(new ScrollingMovementMethod());

        sshManager = SshManager.getInstance();

        String command = getIntent().getStringExtra("command");
        if (command != null && !command.isEmpty()) {
            setTitle(command);
            executeCommand(command);
        } else {
            Toast.makeText(this, "No command to execute.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void executeCommand(String command) {
        progressBar.setVisibility(View.VISIBLE);
        sshManager.runCommand(command, this);
    }

    @Override
    public void onOutputUpdate(String outputChunk) {
        outputTextView.append(outputChunk);
    }

    @Override
    public void onCommandFinished() {
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onError(String error) {
        progressBar.setVisibility(View.GONE);
        outputTextView.setText(error);
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
    }
}