import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class AirportSearchApp1 extends JFrame {
    private static final String RAPIDAPI_HOST = "aerodatabox.p.rapidapi.com";
    private static final String RAPIDAPI_KEY = "e43523db3fmsh87b6c6c7e4c5ebcp19c8d2jsn9323610e3fa1";

    private static final Logger logger = LogManager.getLogger(AirportSearchApp1.class);

    private JTextField latitudeField;
    private JTextField longitudeField;
    private JTextArea resultArea;

    public AirportSearchApp1() {
        setTitle("Airport Finder");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel(new GridLayout(2, 2));
        JLabel latitudeLabel = new JLabel("Szerokość:");
        latitudeField = new JTextField();
        JLabel longitudeLabel = new JLabel("Długość:");
        longitudeField = new JTextField();
        inputPanel.add(latitudeLabel);
        inputPanel.add(latitudeField);
        inputPanel.add(longitudeLabel);
        inputPanel.add(longitudeField);
        add(inputPanel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton searchButton = new JButton("Szukaj");
        searchButton.setPreferredSize(new Dimension(100, 30));
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    searchAirports();
                } catch (IOException ex) {
                    logger.error("Wystąpił błąd podczas wykonywania żądania HTTP.", ex);
                    displayErrorMessage("Wystąpił błąd podczas wykonywania żądania HTTP.");
                } catch (InterruptedException ex) {
                    logger.error("Operacja została przerwana.", ex);
                    displayErrorMessage("Operacja została przerwana.");
                } catch (Exception ex) {
                    logger.error("Wystąpił nieznany błąd.", ex);
                    displayErrorMessage("Wystąpił nieznany błąd.");
                }
            }
        });
        buttonPanel.add(searchButton);
        add(buttonPanel, BorderLayout.CENTER);

        resultArea = new JTextArea();
        resultArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultArea);
        add(scrollPane, BorderLayout.SOUTH);
    }

    private void searchAirports() throws IOException, InterruptedException {
        String latitude = latitudeField.getText();
        String longitude = longitudeField.getText();

        String url = "https://aerodatabox.p.rapidapi.com/airports/search/location?lat=" +
                latitude + "&lon=" + longitude + "&radiusKm=50&limit=10&withFlightInfoOnly=false";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("X-RapidAPI-Key", RAPIDAPI_KEY)
                .header("X-RapidAPI-Host", RAPIDAPI_HOST)
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        JSONObject jsonBody = new JSONObject(response.body());

        JSONArray airportsArray = jsonBody.getJSONArray("items");

        if (airportsArray.length() == 0) {
            logger.warn("Brak dostępnych lotnisk w najbliższym otoczeniu.");
            displayErrorMessage("Brak dostępnych lotnisk w najbliższym otoczeniu.");
            return;
        }

        StringBuilder resultBuilder = new StringBuilder();

        for (int i = 0; i < airportsArray.length(); i++) {
            JSONObject airportObj = airportsArray.getJSONObject(i);

            String airportName = airportObj.getString("name");
            String airportCode = airportObj.getString("iata");
            String airportCountry = airportObj.getString("countryCode");

            resultBuilder.append("Lotnisko: ").append(airportName).append("\n");
            resultBuilder.append("Kod: ").append(airportCode).append("\n");
            resultBuilder.append("Kraj: ").append(airportCountry).append("\n");
            resultBuilder.append("---------------------------------------").append("\n");
        }

        String result = resultBuilder.toString();
        resultArea.setText(result);
        logger.info("Wynik wyszukiwania lotnisk: \n" + result);
    }

    private void displayErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Błąd", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {

                System.setProperty("log4j.configurationFile", "log4j2.xml");
                AirportSearchApp1 app = new AirportSearchApp1();
                app.setVisible(true);
            }
        });
    }
}

