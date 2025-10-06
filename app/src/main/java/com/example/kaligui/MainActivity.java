package com.example.kaligui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ToolAdapter.OnToolClickListener, SshManager.ConnectListener, SshManager.CommandListener {

    private EditText ipAddress, username, password;
    private Button connectButton;
    private TextView output;
    private ProgressBar progressBar;
    private RecyclerView toolsRecyclerView;
    private ToolAdapter toolAdapter;
    private List<Tool> toolList;

    private SshManager sshManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ipAddress = findViewById(R.id.ip_address);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        connectButton = findViewById(R.id.connect_button);
        output = findViewById(R.id.output);
        progressBar = findViewById(R.id.progress_bar);
        toolsRecyclerView = findViewById(R.id.tools_recycler_view);

        output.setMovementMethod(new ScrollingMovementMethod());

        sshManager = new SshManager();

        connectButton.setOnClickListener(v -> {
            if (sshManager.isConnected()) {
                sshManager.disconnect();
                updateUiForDisconnectedState();
            } else {
                progressBar.setVisibility(View.VISIBLE);
                String ip = ipAddress.getText().toString();
                String user = username.getText().toString();
                String pass = password.getText().toString();
                sshManager.connect(user, pass, ip, this);
            }
        });

        setupRecyclerView();
        updateUiForDisconnectedState();
    }

    private void setupRecyclerView() {
        toolsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        toolList = new ArrayList<>();
        toolList.add(new Tool("Nmap Scan", "Scan a target for open ports.", "nmap -sV ", true));
        toolList.add(new Tool("Nikto Scan", "Web server scanner.", "nikto -h ", true));
        toolList.add(new Tool("Dirb", "Web content scanner.", "dirb ", true));
        toolList.add(new Tool("Metasploit", "Exploitation framework.", "msfconsole", false));

        toolAdapter = new ToolAdapter(toolList, this);
        toolsRecyclerView.setAdapter(toolAdapter);
    }

    @Override
    public void onToolClick(Tool tool) {
        if (tool.isRequiresArgument()) {
            showArgumentDialog(tool);
        } else {
            output.setText("");
            progressBar.setVisibility(View.VISIBLE);
            sshManager.runCommand(tool.getCommand(), this);
        }
    }

    private void showArgumentDialog(Tool tool) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(tool.getName());
        builder.setMessage("Enter arguments:");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Run", (dialog, which) -> {
            String arguments = input.getText().toString();
            String fullCommand = tool.getCommand() + arguments;
            output.setText("");
            progressBar.setVisibility(View.VISIBLE);
            sshManager.runCommand(fullCommand, MainActivity.this);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    @Override
    public void onConnected(String message) {
        progressBar.setVisibility(View.GONE);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        updateUiForConnectedState();
    }

    @Override
    public void onOutputUpdate(String outputChunk) {
        output.append(outputChunk);
    }

    @Override
    public void onCommandFinished() {
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onError(String error) {
        progressBar.setVisibility(View.GONE);
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
        output.setText(error);
        if (!sshManager.isConnected()) {
            updateUiForDisconnectedState();
        }
    }

    private void updateUiForConnectedState() {
        connectButton.setText("Disconnect");
        ipAddress.setEnabled(false);
        username.setEnabled(false);
        password.setEnabled(false);
        toolsRecyclerView.setVisibility(View.VISIBLE);
    }

    private void updateUiForDisconnectedState() {
        connectButton.setText("Connect");
        ipAddress.setEnabled(true);
        username.setEnabled(true);
        password.setEnabled(true);
        toolsRecyclerView.setVisibility(View.GONE);
        output.setText("Output will be shown here");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sshManager.disconnect();
    }
}