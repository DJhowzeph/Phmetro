package javafxapplication6;

import com.fazecast.jSerialComm.*;
import com.sun.java.swing.plaf.windows.resources.windows;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import static jdk.nashorn.internal.objects.ArrayBufferView.buffer;

/**
 *
 * @author Saulo Jr
 */
public class FXMLDocumentController implements Initializable {

    @FXML
    private Label lblPh;
    @FXML
    private Button btnConectar;
    @FXML
    private ComboBox cbPortas;
    @FXML
    private SerialPort porta;
    @FXML
    private LineChart<String, Number> chart;
    XYChart.Series<String, Number> graficopH;

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            try {
                Thread.sleep(50);

            } catch (InterruptedException ex) {
                Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
            }
            ler();
        }
    };

    private Thread thread;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        graficopH = new XYChart.Series<>();
        graficopH.setName("pH");
        carregarPortas();
        chart.getData().add(graficopH);
    }

    public void carregarPortas() {
        SerialPort[] portNames = SerialPort.getCommPorts();

        // isso é um for each loop, vai percorrer toda a extensão do array
        for (SerialPort portName : portNames) {
            cbPortas.getItems().add(portName.getSystemPortName());
        }

    }

    @FXML // usa qdo é uma ação
    private void conectar(ActionEvent event) throws InterruptedException, IOException {

        if (btnConectar.getText().equals("Conectar")) { // configurar o botao conectar
            porta = SerialPort.getCommPort(cbPortas.getSelectionModel().getSelectedItem().toString()); // pega o texto dentro da combo box
            porta.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 0, 0); // se pedirmos uma string, e n vier, esperar ate vir
            
            if (porta.openPort()) {
                btnConectar.setText("Desconectar");
                cbPortas.setDisable(true);
                thread = new Thread(runnable);
                thread.start();
            }

        } else { // desconectar da serial port
            porta.closePort();
            cbPortas.setDisable(false);
            btnConectar.setText("Conectar");
            if (thread != null) {
                thread.interrupt();
                
            }
            thread = null;
        }
    }

    public void ler() {
        try (Scanner scanner = new Scanner(porta.getInputStream())) {
             int contador = 1;
            while (porta.isOpen()) {
                while (scanner.hasNextLine()) {
                    try {
                        String line = scanner.nextLine();
                        System.out.println(Double.valueOf(line));
                        Double temp = Double.parseDouble(line);
                        graficopH.getData().add(new XYChart.Data<>(contador + "", temp));
                        Platform.runLater(() -> {
                            lblPh.setText(line);
                            chart.getData().add(graficopH);
                        });
                        contador++;
                    } catch (Exception e) {
                        System.out.println(e.toString());
                    }
                }
                
            }
        } catch (Exception e) {
            porta.closePort();
        }
    }
};
