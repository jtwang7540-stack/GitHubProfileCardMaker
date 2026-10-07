import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Main {
    public static void main(String[] args) {
        // 1. Create the main window frame
        JFrame frame = new JFrame("GitHub Profile Card Maker");
        frame.setSize(450, 220);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new FlowLayout());

        // 2. Add a label, text box, and button
        JLabel label = new JLabel("Enter GitHub Username:");
        JTextField usernameInput = new JTextField(15);
        JButton generateButton = new JButton("Get Stats");

        // 3. Add a label to show the output result
        JLabel resultLabel = new JLabel("Stats will appear here.");

        // 4. Button click event (fetches real data from web)
        generateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameInput.getText().trim();
                if (username.isEmpty()) {
                    resultLabel.setText("Please enter a valid username!");
                    return;
                }

                resultLabel.setText("Fetching data from GitHub...");

                // Run internet request in a background task so the window doesn't freeze
                new Thread(() -> {
                    String stats = fetchGitHubStats(username);
                    resultLabel.setText(stats);
                }).start();
            }
        });

        // 5. Add all parts to window
        frame.add(label);
        frame.add(usernameInput);
        frame.add(generateButton);
        frame.add(resultLabel);

        // 6. Make window visible
        frame.setVisible(true);
    }

    // Helper function that talks to GitHub's server
    private static String fetchGitHubStats(String username) {
        try {
            URL url = new URL("https://api.github.com/users/" + username);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Java-App");

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) { // 200 means SUCCESS
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Extract public repos count from response text
                String json = response.toString();
                if (json.contains("\"public_repos\":")) {
                    int startIndex = json.indexOf("\"public_repos\":") + 15;
                    int endIndex = json.indexOf(",", startIndex);
                    if (endIndex == -1) {
                        endIndex = json.indexOf("}", startIndex);
                    }
                    String repoCount = json.substring(startIndex, endIndex).trim();
                    return "<html><b>User:</b> " + username + "<br><b>Public Repositories:</b> " + repoCount + "</html>";
                }
                return "User found, but couldn't parse repos!";
            } else if (responseCode == 404) {
                return "User not found on GitHub!";
            } else {
                return "Error connecting to GitHub (Code " + responseCode + ")";
            }
        } catch (Exception ex) {
            return "Connection error. Check internet connection!";
        }
    }
}