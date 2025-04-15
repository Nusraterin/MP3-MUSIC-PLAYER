package com.example;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.advanced.AdvancedPlayer;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

public class Mp3player {
    private JFrame frame;
    private JButton previousButton, playButton, nextButton, stopButton;
    private AdvancedPlayer player;
    private Thread playerThread;
    private JLabel gifLabel;
    private JLabel songLabel;
    private JLabel timeLabel;
    private String gifPath = "src/main/resources/guitar-ezgif.com-resize.gif";
    private String playingGifPath = "src/main/resources/7a98413dbf3b22a08914cb78f4064a36-ezgif.com-resize (1).gif";
    private ArrayList<String> filePaths;
    private int currentSongIndex;
    private Timer timer;
    private int elapsedSeconds;

    public Mp3player() {
        filePaths = new ArrayList<>();
        currentSongIndex = -1;

        frame = new JFrame("MP3 Player");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(new Color(255, 228, 225));

        gifLabel = new JLabel(new ImageIcon(gifPath));
        gifLabel.setHorizontalAlignment(SwingConstants.CENTER);
        frame.add(gifLabel, BorderLayout.NORTH);

       
        timeLabel = new JLabel("00:00", SwingConstants.CENTER);
        timeLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        timeLabel.setForeground(new Color(200, 100, 150));

       
        songLabel = new JLabel("No song playing", SwingConstants.CENTER);
        songLabel.setFont(new Font("Arial", Font.BOLD, 16));
        songLabel.setForeground(new Color(252, 142, 172));

        
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(new Color(255, 228, 225));

        timeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        songLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        centerPanel.add(Box.createVerticalStrut(20));
        centerPanel.add(timeLabel);
        centerPanel.add(Box.createVerticalStrut(5));
        centerPanel.add(songLabel);
        frame.add(centerPanel, BorderLayout.CENTER);

       
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(255, 228, 225));
        buttonPanel.setLayout(new FlowLayout());

        ImageIcon previousIcon = new ImageIcon("src/icons/back-button.png");
        ImageIcon playIcon = new ImageIcon("src/icons/play-button-arrowhead.png");
        ImageIcon nextIcon = new ImageIcon("src/icons/next (1).png");
        ImageIcon stopIcon = new ImageIcon("src/icons/stop-button.png");

        previousButton = new JButton(previousIcon);
        playButton = new JButton(playIcon);
        nextButton = new JButton(nextIcon);
        stopButton = new JButton(stopIcon);

        JButton[] buttons = {previousButton, playButton, nextButton, stopButton};
        for (JButton button : buttons) {
            button.setBackground(new Color(255, 228, 225));
            button.setForeground(Color.WHITE);
            button.setBorderPainted(false);
            button.setFocusPainted(false);
            buttonPanel.add(button);
        }

        frame.add(buttonPanel, BorderLayout.SOUTH);

       
        playButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("MP3 Files", "mp3"));

            int result = chooser.showOpenDialog(frame);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = chooser.getSelectedFile();
                File folder = selectedFile.getParentFile();

                File[] mp3Files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".mp3"));

                filePaths.clear();

                if (mp3Files != null) {
                    for (File f : mp3Files) {
                        filePaths.add(f.getAbsolutePath());
                    }

                    for (int i = 0; i < filePaths.size(); i++) {
                        if (filePaths.get(i).equals(selectedFile.getAbsolutePath())) {
                            currentSongIndex = i;
                            break;
                        }
                    }
                    playSelectedSong();
                }
            }
        });

        stopButton.addActionListener(e -> {
            stopMusic();
            songLabel.setText("No song playing");
            gifLabel.setIcon(new ImageIcon(gifPath));
            timeLabel.setText("00:00");
        });

        previousButton.addActionListener(e -> {
            if (!filePaths.isEmpty() && currentSongIndex > 0) {
                currentSongIndex--;
                playSelectedSong();
            }
        });

        nextButton.addActionListener(e -> {
            if (!filePaths.isEmpty() && currentSongIndex < filePaths.size() - 1) {
                currentSongIndex++;
                playSelectedSong();
            }
        });

        frame.setVisible(true);
    }

    private void playSelectedSong() {
        if (currentSongIndex >= 0 && currentSongIndex < filePaths.size()) {
            String path = filePaths.get(currentSongIndex);
            String songName = new File(path).getName();
            songLabel.setText(" " + songName);
            playMusic(path);
        }
    }

    private void playMusic(String filePath) {
        stopMusic();

        try {
            FileInputStream fis = new FileInputStream(filePath);
            player = new AdvancedPlayer(fis);

            gifLabel.setIcon(new ImageIcon(playingGifPath));

            elapsedSeconds = 0;
            updateTimer();
            timer = new Timer(1000, e -> {
                elapsedSeconds++;
                updateTimer();
            });
            timer.start();

            playerThread = new Thread(() -> {
                try {
                    player.play();
                } catch (JavaLayerException e) {
                    e.printStackTrace();
                } finally {
                    if (timer != null) {
                        timer.stop();
                    }
                }
            });
            playerThread.start();

        } catch (IOException | JavaLayerException e) {
            e.printStackTrace();
        }
    }

    private void updateTimer() {
        int minutes = elapsedSeconds / 60;
        int seconds = elapsedSeconds % 60;
        timeLabel.setText(String.format("%02d:%02d", minutes, seconds));
    }

    private void stopMusic() {
        if (player != null) {
            player.close();
        }
        if (playerThread != null && playerThread.isAlive()) {
            playerThread.interrupt();
        }
        if (timer != null) {
            timer.stop();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Mp3player::new);
    }
}
