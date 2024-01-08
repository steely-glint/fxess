package uk.co.westhawk.fxess;

import com.phono.srtplight.Log;
import eu.hansolo.medusa.Gauge;
import eu.hansolo.medusa.GaugeBuilder;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 * JavaFX App
 */
public class App extends Application {

    private static String user;
    private static String pass;
    private static String lat;
    private static String lon;

    private Gauge meter;
    private Gauge charge;
    private Label sun;

    @Override
    public void start(Stage stage) {
        var javaVersion = SystemInfo.javaVersion();
        var javafxVersion = SystemInfo.javafxVersion();
        double h = Math.min(stage.getMaxHeight(), 240);
        double w = Math.min(stage.getMaxWidth(), 320);
        final GridPane grid = new GridPane();
        final GridPane mgrid = new GridPane();
        Log.info("javaFxversion " + javafxVersion);
        Log.info("javaVersion " + javaVersion);
        Function<GaugeBuilder, GaugeBuilder> pctCustomizer = (gb) -> {
            gb = gb.maxValue(100.0)
                    .minValue(0.0)
                    .title("Charge state")
                    .unit("%");
            return gb;
        };
        Function<GaugeBuilder, GaugeBuilder> kwCustomizer = (gb) -> {
            gb = gb.maxValue(5.0)
                    .minValue(-5.0)
                    .title("Power flow")
                    .unit("kw")
                    .decimals(2);

            return gb;
        };
        meter = Meter.make(pctCustomizer);
        charge = Meter.make(kwCustomizer);
        Image image = new Image(getClass().getResourceAsStream("/clearsky_day.png"));
        sun = new Label("Sun Prediction", new ImageView(image));
        sun.setFont(new Font("Arial", 30));
        Label credit = new Label("Weather from https://yr.no");
        credit.setFont(new Font("Arial", 8));

        mgrid.addRow(0, charge, meter);
        grid.addRow(0, mgrid);
        grid.addRow(1, sun);
        grid.addRow(2, credit);

        StackPane stack = new StackPane(grid);
        var scene = new Scene(stack, w, h);
        stage.setScene(scene);
        stage.show();
        startStats();
    }

    public static void main(String[] argv) {
        Log.setLevel(Log.INFO);

        if (argv.length != 4) {
            Log.error("Need account details and location");
            try {
                Thread.sleep(30000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            System.exit(1);
        }
        user = argv[0];
        pass = argv[1];
        lat = argv[2];
        lon = argv[3];
        launch();
    }

    void startStats() {
        final Data data = new Data(user, pass);
        ScheduledExecutorService ex = Executors.newSingleThreadScheduledExecutor();

        NextSun ns = new NextSun(Double.valueOf(lat), Double.valueOf(lon));
        ex.scheduleAtFixedRate(() -> {
            try {
                var bat = data.getBattery();
                Log.info("bat " + bat.toString());
                var s = ns.getSuns();
                Platform.runLater(() -> {
                    meter.setValue(bat.soc);
                    charge.setValue(bat.power);
                    sun.setText(s.toString());
                });
            } catch (Throwable x) {
                Log.error("problen with getting data " + x.getMessage());
                x.printStackTrace();
            }
        }, 0, 5, TimeUnit.MINUTES);
    }
}
