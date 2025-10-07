package com.example.kaligui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ToolAdapter.OnToolClickListener, ToolAdapter.OnToolLongClickListener, SshManager.ConnectListener {

    private EditText ipAddress, username, password, manualCommand;
    private Button connectButton, updateButton, upgradeButton, runManualCommandButton;
    private ProgressBar progressBar;
    private RecyclerView toolsRecyclerView;
    private ToolAdapter toolAdapter;
    private List<Tool> toolList;
    private LinearLayout systemButtonsLayout, manualCommandLayout;

    private SshManager sshManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        ipAddress = findViewById(R.id.ip_address);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        manualCommand = findViewById(R.id.manual_command);
        connectButton = findViewById(R.id.connect_button);
        updateButton = findViewById(R.id.update_button);
        upgradeButton = findViewById(R.id.upgrade_button);
        runManualCommandButton = findViewById(R.id.run_manual_command_button);
        progressBar = findViewById(R.id.progress_bar);
        toolsRecyclerView = findViewById(R.id.tools_recycler_view);
        systemButtonsLayout = findViewById(R.id.system_buttons_layout);
        manualCommandLayout = findViewById(R.id.manual_command_layout);

        sshManager = SshManager.getInstance();

        // Setup Listeners
        connectButton.setOnClickListener(v -> handleConnection());
        updateButton.setOnClickListener(v -> runUpdate());
        upgradeButton.setOnClickListener(v -> runUpgrade());
        runManualCommandButton.setOnClickListener(v -> runManualCommand());

        setupRecyclerView();
        updateUiForDisconnectedState();
    }

    private void handleConnection() {
        if (sshManager.isConnected()) {
            sshManager.disconnect();
            updateUiForDisconnectedState();
            Toast.makeText(this, "Disconnected", Toast.LENGTH_SHORT).show();
        } else {
            progressBar.setVisibility(View.VISIBLE);
            String ip = ipAddress.getText().toString();
            String user = username.getText().toString();
            String pass = password.getText().toString();
            sshManager.connect(user, pass, ip, this);
        }
    }

    private void runUpdate() {
        showSudoWarningDialog(() -> {
            String pass = password.getText().toString();
            String command = "echo '" + pass + "' | sudo -S apt-get update";
            executeCommand(command);
        });
    }

    private void runUpgrade() {
        showSudoWarningDialog(() -> {
            String pass = password.getText().toString();
            String command = "echo '" + pass + "' | sudo -S apt-get upgrade -y";
            executeCommand(command);
        });
    }

    private void showSudoWarningDialog(Runnable onConfirm) {
        new AlertDialog.Builder(this)
                .setTitle("Security Warning")
                .setMessage("This action requires sudo privileges. The current method of passing the password is not secure and may expose your password in the server's command history.\n\nDo you want to proceed?")
                .setPositiveButton("Proceed", (dialog, which) -> onConfirm.run())
                .setNegativeButton("Cancel", (dialog, which) -> dialog.cancel())
                .show();
    }

    private void runManualCommand() {
        String command = manualCommand.getText().toString();
        if (!command.isEmpty()) {
            executeCommand(command);
        } else {
            Toast.makeText(this, "Command cannot be empty", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupRecyclerView() {
        toolsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        toolList = new ArrayList<>();
        toolAdapter = new ToolAdapter(toolList, this, this);
        toolsRecyclerView.setAdapter(toolAdapter);
    }

    private void fetchToolList() {
        progressBar.setVisibility(View.VISIBLE);
        String command = "ls /usr/bin/ /bin/ /sbin/ | sort -u";
        sshManager.getCommandOutput(command, new SshManager.CommandOutputListener() {
            @Override
            public void onComplete(String result) {
                progressBar.setVisibility(View.GONE);
                List<String> tools = new ArrayList<>(Arrays.asList(result.split("\n")));
                toolList.clear();
                for (String toolName : tools) {
                    if (!toolName.trim().isEmpty()) {
                        toolList.add(new Tool(toolName.trim(), "Kali Tool", toolName.trim() + " ", true));
                    }
                }
                toolAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "Failed to fetch tool list: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onToolClick(Tool tool) {
        if (tool.isRequiresArgument()) {
            showArgumentDialog(tool);
        } else {
            executeCommand(tool.getCommand());
        }
    }

    @Override
    public void onToolLongClick(Tool tool) {
        progressBar.setVisibility(View.VISIBLE);
        String command = "man " + tool.getName();
        sshManager.getCommandOutput(command, new SshManager.CommandOutputListener() {
            @Override
            public void onComplete(String result) {
                progressBar.setVisibility(View.GONE);
                showManPageDialog(tool.getName(), result);
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "Could not load man page: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showManPageDialog(String toolName, String manPageContent) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("man " + toolName);

        final TextView manPageTextView = new TextView(this);
        manPageTextView.setText(manPageContent);
        manPageTextView.setMovementMethod(new ScrollingMovementMethod());
        manPageTextView.setPadding(50, 20, 50, 20);

        builder.setView(manPageTextView);

        builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());
        builder.show();
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
            executeCommand(fullCommand);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void executeCommand(String command) {
        Intent intent = new Intent(this, OutputActivity.class);
        intent.putExtra("command", command);
        startActivity(intent);
    }

    @Override
    public void onConnected(String message) {
        progressBar.setVisibility(View.GONE);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        updateUiForConnectedState();
        fetchToolList();
    }

    @Override
    public void onError(String error) {
        progressBar.setVisibility(View.GONE);
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
        if (!sshManager.isConnected()) {
            updateUiForDisconnectedState();
        }
    }

    private void updateUiForConnectedState() {
        connectButton.setText("Disconnect");
        ipAddress.setEnabled(false);
        username.setEnabled(false);
        password.setEnabled(false);
        systemButtonsLayout.setVisibility(View.VISIBLE);
        manualCommandLayout.setVisibility(View.VISIBLE);
        toolsRecyclerView.setVisibility(View.VISIBLE);
    }

    private void updateUiForDisconnectedState() {
        connectButton.setText("Connect");
        ipAddress.setEnabled(true);
        username.setEnabled(true);
        password.setEnabled(true);
        systemButtonsLayout.setVisibility(View.GONE);
        manualCommandLayout.setVisibility(View.GONE);
        if (toolList != null) {
            toolList.clear();
            toolAdapter.notifyDataSetChanged();
        }
        toolsRecyclerView.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sshManager.disconnect();
    }
}