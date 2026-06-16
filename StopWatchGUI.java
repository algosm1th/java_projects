import javax.swing.*;
import java.awt.*;
import java.util.Random;

//StopWatch

class StopWatch {

    private int seconds;
    private int minutes;
    private int hours;
    private boolean running;

    StopWatch() {
        seconds = minutes = hours = 0;
        running = false;
    }

    public void start() {
        running = true;
    }
    public void stop() {
        running = false;
    }

    public void reset() {
        hours = minutes = seconds = 0;
    }

    public void tick() {
        if (running) {
            seconds++;
            if (seconds == 60) {
                seconds = 0;
                minutes++;
            }
            if (minutes == 60) {
                minutes = 0;
                hours++;
            }
        }
    }

    public String getTime() {
        return String.format("%02d : %02d : %02d", hours, minutes, seconds);
    }
}

//Floating Digits Background

class FloatingDigitsPanel extends JPanel{

    public static class Digit{
        int x,y,speed;
        char value;
    }

    private Digit[] digits;
    private Random rand = new Random();
    FloatingDigitsPanel(){

        digits = new Digit[45];
        for(int i = 0; i< digits.length ; i++) {
            digits[i] = new Digit();
            resetDigit(digits[i]);
        }
        Timer animationTimer = new Timer(40, e -> {
            for (Digit d : digits) {
                d.y -= d.speed;
                if (d.y < 0) {
                    resetDigit(d);
                    d.y = getHeight();
                }
            }
            repaint();
        });
        animationTimer.start();
    }

    private void resetDigit(Digit d) {
        d.x = rand.nextInt(1800);
        d.y = rand.nextInt(900);
        d.speed = 1 + rand.nextInt(2);
        d.value = (char) ('0' + rand.nextInt(10));
    }

    @Override //from JComponent
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        g2.setFont(new Font("Consolas", Font.BOLD, 40));
        g2.setColor(new Color(5, 173,181, 40)); // translucent digits

        for (Digit d : digits) {
            g2.drawString(String.valueOf(d.value), d.x, d.y);
        }
    }
}

//GUI

public class StopWatchGUI {

    public static void main(String[] args) {

        JFrame frame = new JFrame("Stop Watch");
        frame.setSize(1080, 900);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        FloatingDigitsPanel background = new FloatingDigitsPanel();
        frame.setContentPane(background);
        frame.setLayout(null);

        //Labels

        JLabel label = new JLabel("ChronoX");
        label.setBounds(500, 90, 400, 100);
        label.setFont(new Font("Georgia", Font.BOLD, 60));
        label.setForeground(new Color(0, 173,181));
        background.add(label);

        JLabel statement = new JLabel("- every second hits different -");
        statement.setBounds(530, 160, 400, 40);
        statement.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        statement.setForeground(new Color(90, 90, 90));
        background.add(statement);

        JLabel timeLabel = new JLabel("00 : 00 : 00");
        timeLabel.setBounds(430, 260, 500, 70);
        timeLabel.setFont(new Font("Courier New", Font.PLAIN, 60));
        timeLabel.setForeground(Color.black);
        background.add(timeLabel);

        //Buttons

        JToggleButton toggle = new JToggleButton("Start");
        toggle.setBounds(350, 400, 150, 50);
        toggle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        toggle.setFocusPainted(false);
        toggle.setBorder(BorderFactory.createLineBorder(new Color(0, 122, 204), 2));
        background.add(toggle);

        JButton reset = new JButton("Reset");
        reset.setBounds(550, 400, 150, 50);
        reset.setFont(new Font("Segoe UI", Font.BOLD, 18));
        reset.setFocusPainted(false);
        reset.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        background.add(reset);

        JButton exit = new JButton("Exit");
        exit.setBounds(750, 400, 150, 50);
        exit.setFont(new Font("Segoe UI", Font.BOLD, 18));
        exit.setForeground(new Color(220, 53, 69));
        exit.setFocusPainted(false);
        exit.setBorder(BorderFactory.createLineBorder(new Color(220, 53, 69), 2));
        background.add(exit);

        //Logic portion

        StopWatch stopwatch = new StopWatch();

        Timer timer = new Timer(1000, e -> {
            stopwatch.tick();
            timeLabel.setText(stopwatch.getTime());
        });
        timer.start();

        toggle.addActionListener(e -> {
            if (toggle.isSelected()) {
                toggle.setText("Stop");
                toggle.setForeground(new Color(220, 53, 69));
                stopwatch.start();
            } else {
                toggle.setText("Start");
                toggle.setForeground(Color.BLACK);
                stopwatch.stop();
            }
        });

        reset.addActionListener(e -> {
            stopwatch.reset();
            timeLabel.setText(stopwatch.getTime());
        });

        exit.addActionListener(e -> System.exit(0));

        frame.setVisible(true);
    }
}

