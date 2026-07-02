import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class Digitalclock extends Application {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh : mm : ss");
    public static void main(String[] args){
    launch(args);
    }

    public void start(Stage stage){
        FloatingDigits background = new FloatingDigits(1600,800);
        Label clockLabel = new Label();
        clockLabel.setStyle(
                    "-fx-font-family: Impact;"+
                        "-fx-font-size : 234;"+
                        "-fx-text-fill: #f5f;"
        );
        update(clockLabel);

        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1),e -> update(clockLabel)));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        StackPane root = new StackPane(clockLabel);
        root.setStyle("-fx-background-color : #000");
        root.getChildren().add(background);
        Scene scene = new Scene(root);
        stage.setTitle("Digital Clock");
        stage.setMaxHeight(800);
        stage.setMaxWidth(1600);
        stage.setScene(scene);
        stage.show();

    }
    public void update(Label label){
        label.setText(LocalTime.now().format(formatter));
    }
}

class FloatingDigits extends Pane {

    double canvasWidth = 1600;
    double canvasHeight = 800;
    Canvas canvas;
    GraphicsContext gc;
    Digit[] digit = new Digit[40];
    Random random = new Random();
    private class Digit {
        int x;
        int y;
        int speed;
        char value;
    }

    FloatingDigits(double width, double height) {
        canvas = new Canvas(width,height);
        gc = canvas.getGraphicsContext2D();
        getChildren().add(canvas);
        for(int i = 0; i < digit.length ; i++){
            digit[i] = new Digit();
            resetDigit(digit[i]);
        }
        AnimationTimer timer = new AnimationTimer() {
            long lastUpdate = 0;
            @Override
            public void handle (long now) {
                if (now - lastUpdate < 40_000_000) return;
                lastUpdate = now;
                for ( Digit d : digit){
                    d.y -= d.speed;
                    if ( d.y < 0){
                        resetDigit(d);
                        d.y = (int) getHeight();
                    }
                }
                draw();
            }
        };
        timer.start();

    }
    public void resetDigit(Digit d){
        d.x = (int)random.nextInt(1920);
        d.y = random.nextInt(1000);
        d.speed = 1 + random.nextInt(2);
        d.value = (char)('0' + random.nextInt(10));
    }
    public void draw(){
        gc.clearRect(0,0,canvasWidth, canvasHeight);
        gc.setFont(Font.font("Consolas", FontWeight.BOLD, 80));
        gc.setFill(Color.rgb(255, 55, 255, 60 / 255.0));

        for( Digit d : digit){
            gc.fillText(String.valueOf(d.value),d.x,d.y);
        }
    }
}
