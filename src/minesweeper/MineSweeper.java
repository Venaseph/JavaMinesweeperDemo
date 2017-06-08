package minesweeper;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import javax.swing.*;
import sun.audio.AudioPlayer;
import sun.audio.AudioStream;

// C.Peragine CP337 - Java - Spring 2017
public class MineSweeper extends JFrame implements MouseListener{
    //components and set up for main JFrame
    private final int WIDTHS = 565, HEIGHTS = 642;  // Starting Window Size Constraints & Panels  
    private JTabbedPane topFrame;
    private JPanel tfp1, tfp2, tfp3, tfp4, tfp5;
    private Color BGC = new Color(238,238,238);
    private Font BFF = new Font("Arial",Font.BOLD,12); // title border font
    
    //components for about tab
    private JTextArea about;
    
    //components for instructions tab
    private JTextArea instruct1, instruct2, instruct3;
    
    //components for statistics tab
    private GridBagLayout sGBL;
    private GridBagConstraints sGBC;
    private JPanel[] sPanels;
    private int[][] sConstraints;
    private String[] sTitle;
    private JScrollPane[] sScrollPane;
    private JTextArea[] sTextArea;
    
    //components for options tab
    private JPanel diffPane, soundsPane;
    private ButtonGroup diffBGroup, soundsGroup;
    private JRadioButton[] diffRadio, soundsRadio;
    private String[] diffTitles, soundsTitles;   
    
    //components for game tab
    private GridBagLayout gGBL;
    private GridBagConstraints gGBC;
    private JPanel gP1, gP2;
        
    // components for gP1/HUD (Heads Up Display)
    private GridBagLayout hGBL;
    private GridBagConstraints hGBC;
    private int[][] hConstraints;
    private JPanel[] HUD;
    private JButton newGame;
    private JLabel[] hudTFS;
    private ImageIcon icMine, icClock;
    private JLabel jlMine, jlClock;
    private int mineCount = 10;
    
    //components for Game Board   
    protected int gridLength = 10;
    protected JButton[][] gButtons;
    private boolean[][] hasMineE, hasMineN, hasMineH, hasMine[];
    private ArrayList<Integer> X, Y;
    
    //components for indicators
    private int iC; // indicator count
    private int[][] indicators;
    private Font if1 = new Font("Arial",Font.BOLD,18);
    
    //components for flags
    private int flagCount = mineCount;
    
    //subclass for timer thread and related components, didn't use runnable since I'm not thread pooling but could have
    private class Timer extends Thread {
        @Override
        public void run() {
            startTime = System.currentTimeMillis();
            do  {
                long elapsedTime = System.currentTimeMillis() - startTime;
                long elapsedSeconds = elapsedTime / 1000;  // to seconds conversion 
                hudTFS[1].setText(Long.toString(elapsedSeconds));  // update timer
            }while(firstClick == false); // stop condition            
        }
    }
    private boolean firstClick = true;
    private long startTime;
    private long elapsedSeconds;
    
    
    public MineSweeper(String title) {  // Constructor
        super(title);  // sets Window Name
        setPreferredSize(new Dimension(WIDTHS, HEIGHTS)); // set height-width
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Clean close of program Frame
        setResizable(false); // Allow resizing
        
        // Change frame icon NOTE: didn't screw with a wrapper to change the .jar icon
        Image gameIcon = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/images/AppIcon.png"));
        setIconImage(gameIcon);

        createTabbedPane();
        createAboutPane();
        createInstructionsPane();
        createStatsPane();
        createOptionsPane();
        createGamePane();
        createHUD();
        createGameBoard();
        setMineLocations();
        
        
        pack(); // pack frame
        setLocationRelativeTo(null); // centers on screen
        setVisible(true); // makes all visible
        
    }
    
    private void createTabbedPane() {
        topFrame = new JTabbedPane();  // instan TabbedPane
        topFrame.setFont(new Font(null, Font.PLAIN|Font.ITALIC, 12 ));  // set font style on panes
        
        tfp1 = new JPanel();  // instan tab 1
        tfp2 = new JPanel();  // instan tab 2
        tfp3 = new JPanel();  // instan tab 3
        tfp4 = new JPanel();  // instan tab 4
        tfp5 = new JPanel();  // instan tab 5
        
        topFrame.add("Game",tfp1);  // add panels to topFrame
        topFrame.add("Options",tfp2);
        topFrame.add("Statistics",tfp3);
        topFrame.add("Instructions",tfp4);
        topFrame.add("About",tfp5);
        add(topFrame);       
    }
    
    private void createAboutPane() {
        tfp5.setLayout(new BorderLayout());  // set layout for about pane
        about = new JTextArea();  // instan new text area
        about.setEditable(false); // locked from user
        about.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "About",0,0,BFF));  //set border with customer font
        about.setFont(new Font("Arial", Font.PLAIN, 12)); // adjust font in about textarea
        about.setBackground(BGC);  // set background to match rest of app
        readTxt("/texts/about.txt", about);
        tfp5.add(about);  // adds to tab panel
    }
    
    private void createInstructionsPane() {
        tfp4.setLayout(new GridLayout(3,1));
        instruct1 = new JTextArea();
        instruct1.setEditable(false);
        instruct1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Instructions For MineSweeper",0,0,BFF));
        instruct1.setFont(new Font("Arial", Font.PLAIN, 12));
        instruct1.setBackground(BGC);
        readTxt("/texts/instruct1.txt", instruct1);
        tfp4.add(instruct1);
        
        instruct2 = new JTextArea();
        instruct2.setEditable(false);
        instruct2.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Finer Details Of Sweeping",0,0,BFF));
        instruct2.setFont(new Font("Arial", Font.PLAIN, 12));
        instruct2.setBackground(BGC);
        readTxt("/texts/instruct2.txt", instruct2);
        tfp4.add(instruct2);
        
        instruct3 = new JTextArea();
        instruct3.setEditable(false);
        instruct3.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "The Status Indicators Do What?",0,0,BFF));
        instruct3.setFont(new Font("Arial", Font.PLAIN, 12));
        instruct3.setBackground(BGC);
        readTxt("/texts/instruct3.txt", instruct3);
        tfp4.add(instruct3);
    }
    
    private void readTxt(String file, JTextArea area) {
        try { // method to read in txt files for Instruction Panes           
            FileReader fr = new FileReader(file);
            BufferedReader reader = new BufferedReader(fr);
            area.read(reader, "");
        } catch (IOException ioe) {
            System.err.println(ioe);
        } 
    }    

    private void createStatsPane() {
        createSGBL(); // method to create gbl for stats tab
        tfp3.setLayout(sGBL);
        sConstraints = new int [][] {{0,0,2,1},{0,1,1,1},{1,1,1,1},{0,2,1,1},{1,2,1,1},{0,3,1,1},{1,3,1,1}};  // mda for grid bag constraints to create panels
        sTitle = new String[]{"High Scores", "Win Rate", "Quickest Game", "Games Played", "Games Won", "Longest Win Streak", "Longest Losing Streak"}; // string for 
        sPanels = new JPanel[7];
        sTextArea = new JTextArea[7];
        sScrollPane = new JScrollPane[7];
        
        for(int i =0; i < sPanels.length; i++) {
            sPanels[i] = new JPanel(new BorderLayout());
            sPanels[i].setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), sTitle[i], 0, 0, BFF));
            sGBC.gridx = sConstraints[i][0];
            sGBC.gridy = sConstraints[i][1];
            sGBC.gridwidth = sConstraints[i][2];
            sGBC.gridheight = sConstraints[i][3];
            sTextArea[i] = new JTextArea();
            sScrollPane[i] = new JScrollPane(sTextArea[i]);
            if(i == 0) {
                sScrollPane[i].setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            } else {
                sScrollPane[i].setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
            }
            sTextArea[i].setEditable(false);
            sTextArea[i].setBackground(new Color(216,231,255));
            sPanels[i].add(sScrollPane[i]);
            tfp3.add(sPanels[i], sGBC);
        }
    }
    
    private void createSGBL() {
        sGBL = new GridBagLayout(); //  instan new GridBagLayout
        sGBL.columnWidths = new int[] {278,278}; // sets widths of grid columns
        sGBL.rowHeights = new int[] {285, 100, 100, 100}; // sets height of grid rows       
        sGBC = new GridBagConstraints();  // instan new GridBagConstraints
        sGBC.fill = GridBagConstraints.BOTH; // fill display area entirely
    }
    
    private void createOptionsPane() {
        tfp2.setLayout(new GridLayout(8,1)); //layout for options pane
        diffPane = new JPanel(new FlowLayout());  // difficulty pane
        diffPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Choose a Difficulty:",0,0,BFF));
        diffPane.setBackground(BGC);
        
        diffBGroup = new ButtonGroup(); // Button Group for Difficulty Radio Buttons
        diffRadio = new JRadioButton[3];
        diffTitles = new String[]{"Easy", "Normal", "Hard"};
        for(int i = 0; i < diffRadio.length; i++) {
            diffRadio[i] = new JRadioButton(diffTitles[i]);
            diffRadio[i].addMouseListener(this);
            diffBGroup.add(diffRadio[i]);
            diffPane.add(diffRadio[i]);
        }
        tfp2.add(diffPane);
        diffRadio[0].setSelected(true);
        
        // Add pane to warn game reset on change
        
        soundsPane = new JPanel(new FlowLayout());  // difficulty pane
        soundsPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Sound Settings:",0,0,BFF));
        soundsPane.setBackground(BGC);
        soundsGroup = new ButtonGroup(); // Button Group for Difficulty Radio Buttons
        soundsRadio = new JRadioButton[2];
        soundsTitles = new String[]{"Sounds On", "Sounds Off"};
        for(int i = 0; i < soundsRadio.length; i++) {
            soundsRadio[i] = new JRadioButton(soundsTitles[i]);
            soundsRadio[i].addMouseListener(this);
            soundsGroup.add(soundsRadio[i]);
            soundsPane.add(soundsRadio[i]);
        }
        tfp2.add(soundsPane);
        soundsRadio[0].setSelected(true);
    }
    
    private void createGamePane() {
        creategGBL();
        tfp1.setLayout(gGBL);
        
        gP1 = new JPanel();  // panel for timers, mines, new game, etc
        gGBC.gridx = 0; gGBC.gridy = 0; gGBC.gridwidth = 1; gGBC.gridheight = 1;
        tfp1.add(gP1, gGBC);
        
        gP2 = new JPanel();  // panel for game board
        gGBC.gridx = 0; gGBC.gridy = 1; gGBC.gridwidth = 1; gGBC.gridheight = 1;
        tfp1.add(gP2, gGBC);
    }
    
    private void creategGBL() {
        gGBL = new GridBagLayout(); //  instan new GridBagLayout
        gGBL.columnWidths = new int[] {500}; // sets widths of grid columns
        gGBL.rowHeights = new int[] {75, 500}; // sets height of grid rows       
        gGBC = new GridBagConstraints();  // instan new GridBagConstraints
        gGBC.fill = GridBagConstraints.BOTH; // fill display area entirely
    }    
    
    private void createHUD() {
        createhGBL();
        gP1.setLayout(hGBL);
        hConstraints = new int [][] {{0,0,1,1},{1,0,1,1},{2,0,1,1},{3,0,1,1},{4,0,1,1}};
        
        HUD = new JPanel[5];
        for(int i =0; i < HUD.length; i++) {
            HUD[i] = new JPanel(new BorderLayout());
            hGBC.gridx = hConstraints[i][0];
            hGBC.gridy = hConstraints[i][1];
            hGBC.gridwidth = hConstraints[i][2];
            hGBC.gridheight = hConstraints[i][3];
            gP1.add(HUD[i], hGBC);
        }
        // Set icons for mines and clock in HUD
        icMine = new ImageIcon(this.getClass().getResource("/images/HUDClock.png"));
        jlMine = new JLabel(icMine);
        HUD[4].add(jlMine);    
    
        icClock = new ImageIcon(this.getClass().getResource("/images/HUDMine.png"));
        jlClock = new JLabel(icClock);
        HUD[0].add(jlClock);
    
        // Set JLabels for Mines/Timer
        hudTFS = new JLabel[2];
        for(int j=0; j < hudTFS.length; j++) {
            hudTFS[j] = new JLabel();
            hudTFS[j].setFont(new Font("Arial", Font.PLAIN, 25));
            hudTFS[j].setForeground(Color.DARK_GRAY);
            hudTFS[j].setHorizontalAlignment(SwingConstants.CENTER);
        } 
        hudTFS[0].setText(Integer.toString(flagCount));
        HUD[1].add(hudTFS[0]);
        
        hudTFS[1].setText("0");
        HUD[3].add(hudTFS[1]);
    
        newGame = new JButton("New Game");
        newGame.setFont(BFF);
        newGame.setForeground(Color.blue);

        newGame.addMouseListener(this);
        HUD[2].add(newGame);
    }
    
    private void createhGBL() {
        hGBL = new GridBagLayout(); //  instan new GridBagLayout
        hGBL.columnWidths = new int[] {75,75,250,75,75}; // sets widths of grid columns
        hGBL.rowHeights = new int[] {25}; // sets height of grid rows       
        hGBC = new GridBagConstraints();  // instan new GridBagConstraints
        hGBC.fill = GridBagConstraints.BOTH; // fill display area entirely
    }    
    
    public void createGameBoard() {
        gP2.setLayout(new GridLayout(gridLength,gridLength,-1,-1));
        gButtons = new JButton[gridLength+1][gridLength+1];  // +1's deal with bounds issues on check logic and boolean[][]
        for (int i = 1; i < gridLength+1; i++) {
            for (int j = 1; j < gridLength+1; j++) {
                gButtons[i][j] = new JButton();
                gButtons[i][j].setBackground(Color.LIGHT_GRAY);
                gButtons[i][j].setMargin(new Insets(0,0,0,0));
                gButtons[i][j].setFont(if1);
                gButtons[i][j].addMouseListener(this);
                gP2.add(gButtons[i][j]);
            }
        }
    }
    
    private void setMineLocations() {
        setGridVars();
        X = new ArrayList<>(); X.clear(); // ArrayLists to handle various difficulties, clears it for each new game
        Y = new ArrayList<>(); Y.clear();
        do { 
            for(int i = 1; i < gridLength+1; i++) {
                X.add(i);  // adds numbers to each element, as int
                Y.add(i);
            }
        } while (mineCount*2 >= X.size());  // bounds starts at 0, to make sure enough variables are created for mines needed and dup handling
        
        Collections.shuffle(X); Collections.shuffle(Y);  // mixes up array values
        
        if (diffRadio[0].isSelected()) {  // diff is easy
            hasMineE = new boolean [gridLength+2][gridLength+2];  // instan MDA for mines
            for(int j = 0; j < mineCount; j++) {
                    hasMineE[X.get(j)][Y.get(j)] = true; 
                }                   
        setIndicators(hasMineE);
        
        } else if (diffRadio[1].isSelected()) {
            hasMineN = new boolean [gridLength+2][gridLength+2];  // instan MDA for mines       
            int runCount = mineCount;
            for(int j = 0; j < runCount; j++) {
		if(hasMineN[X.get(j)][Y.get(j)]) {  // dup watch/correction
                    runCount++;
		} else {
                    hasMineN[X.get(j)][Y.get(j)] = true; 
                }               
            }
        setIndicators(hasMineN);
        
        } else if (diffRadio[2].isSelected()) {
            hasMineH = new boolean [gridLength+2][gridLength+2];  // instan MDA for mines       
            int runCount = mineCount;
            for(int j = 0; j < runCount; j++) {
		if(hasMineH[X.get(j)][Y.get(j)]) {  // dup watch/correction, mine assignment runs an extra time for each dup
                    runCount++;
		} else {
                    hasMineH[X.get(j)][Y.get(j)] = true; 
                }               
            }
        setIndicators(hasMineH);     
        }
    }
    
    private void setGridVars() {  // sets variables for grid and mines when redrawn based on diffRadio selection
        if (diffRadio[0].isSelected()) {
            gridLength = 10; mineCount = 10;
        } else if (diffRadio[1].isSelected()) {
            gridLength = 14; mineCount = 40;
        } else if (diffRadio[2].isSelected()) {
            gridLength = 17; mineCount = 60;
        } flagCount = mineCount;     
    }
    
    private void setIndicators(boolean[][] hasMine) {  // calculate mines that touch each gButton
        indicators = new int[19][19]; //set two spaces larger than Hard difficulty to avoid bounds issues with hasMine boolean values outside [][] range?
        iC = 0;
        for(int i = 1; i < gridLength+1; i++) {
            for(int j = 1; j < gridLength+1; j++) {
                if(hasMine[i][j]) {
                    //do nothing
                } else if (hasMine[i][j] == false) {
                    if(hasMine[i-1][j-1]){iC++;} if(hasMine[i][j-1]){iC++;} if(hasMine[i+1][j-1]){iC++;}
                    if(hasMine[i-1][j]){iC++;} if(hasMine[i+1][j]){iC++;}
                    if(hasMine[i-1][j+1]){iC++;} if(hasMine[i][j+1]){iC++;} if(hasMine[i+1][j+1]){iC++;}
                    indicators[i][j] = iC; iC =0; 
                }
            }
        }
    }
    
    private void indicatorColor(int i, int j) {  // handles indicator color values
        if(indicators[i][j] == 1){
            gButtons[i][j].setForeground(Color.BLUE); 
        } else if (indicators[i][j] == 2) {
            gButtons[i][j].setForeground(Color.GREEN);           
        } else if (indicators[i][j] == 3) {
            gButtons[i][j].setForeground(Color.RED);           
        } else if (indicators[i][j] >= 4) {
            gButtons[i][j].setForeground(new Color(128, 0, 128));            
        } else if (indicators[i][j] >= 5) {
            gButtons[i][j].setForeground(new Color(128, 0, 0));            
        } else if (indicators[i][j] >= 6) {
            gButtons[i][j].setForeground(new Color(64,224,208));            
        } else if (indicators[i][j] >= 7) {
            gButtons[i][j].setForeground(Color.PINK);            
        } else if (indicators[i][j] >= 8) {
            gButtons[i][j].setForeground(Color.BLACK);            
        }
    }
    
    private void clickLogic(boolean[][] hm, int i, int j) {
        if (hm[i][j]) {  // if you hit a bomb                           
            endGameLoss(hm,i,j); 
        } else {
            mineChain(i,j);  // Recursive Checking
            checkWin(hm);  // win stuff
        }
    }
    
    public void mineChain(int i, int j) {  // recursive mineChecker
        if (i < 1 || i > gridLength || j < 1 || j > gridLength) // if out of bounds
            return;
        {
            if(gButtons[i][j].getIcon() != null) { // check for flags first and remove
                gButtons[i][j].setIcon(null);    // remove it
                flagCount++; hudTFS[0].setText(Integer.toString(flagCount));  // update flags out
            }
            if (indicators[i][j] == 0 && gButtons[i][j].isEnabled()) { // if empty and unchecked
                gButtons[i][j].removeMouseListener(this);
                gButtons[i][j].setIcon(null);
                gButtons[i][j].setEnabled(false);
                mineChain(i-1,j-1); mineChain(i,j-1); mineChain(i+1,j-1); // tops
                mineChain(i-1,j); mineChain(i+1,j); // mids
                mineChain(i-1,j+1); mineChain(i,j+1); mineChain(i+1,j+1);  // bottoms
            } else if (indicators[i][j] != 0) { // if not empty
                gButtons[i][j].removeMouseListener(this);
                gButtons[i][j].setIcon(null);
                gButtons[i][j].setText(String.valueOf(indicators[i][j]));  // sets text to value of indicators on button
                indicatorColor(i,j);
               return;
            }
        }       
    }
    
    private void flagToggle(int i, int j) {
        if (flagCount > 0) { // condition to block negative flagging
            playFlagSound();
            if(gButtons[i][j].getIcon() == null) { // if button isn't flagged
                if(diffRadio[0].isSelected() || diffRadio[1].isSelected()) {
                    gButtons[i][j].setIcon(new ImageIcon(getClass().getResource("/images/flag10.png"))); // add one for Easy & Normal
                    flagCount--; hudTFS[0].setText(Integer.toString(flagCount));  // update flags out 
                } else if (diffRadio[2].isSelected()) {
                    gButtons[i][j].setIcon(new ImageIcon(getClass().getResource("/images/flag60.png")));  // add one for Hard
                    flagCount--; hudTFS[0].setText(Integer.toString(flagCount));  // update flags out
                }    
            } else {
                gButtons[i][j].setIcon(null);    // remove it
                flagCount++; hudTFS[0].setText(Integer.toString(flagCount));  // update flags out
            }
        } else if (flagCount == 0 && gButtons[i][j].getIcon() != null) {  // handles zero value issues with flagging
            playFlagSound();
            gButtons[i][j].setIcon(null);    // remove it
            flagCount++; hudTFS[0].setText(Integer.toString(flagCount));  // update flags out
        } else if (flagCount == 0 && gButtons[i][j].getIcon() == null) {
            playFlagLimitSound();
        }
    }
    
    private void playWinSound() {
        if(soundsRadio[0].isSelected()) {
            try {
                InputStream inputStream = getClass().getResourceAsStream("/sounds/win.wav");
                AudioStream audioStream = new AudioStream(inputStream);
                AudioPlayer.player.start(audioStream);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    private void playLoseSound() {
        if(soundsRadio[0].isSelected()) {
            try {
                InputStream inputStream = getClass().getResourceAsStream("/sounds/lose.wav");
                AudioStream audioStream = new AudioStream(inputStream);
                AudioPlayer.player.start(audioStream);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void playFlagSound() {
        if(soundsRadio[0].isSelected()) {
            try {
                InputStream inputStream = getClass().getResourceAsStream("/sounds/flag.wav");
                AudioStream audioStream = new AudioStream(inputStream);
                AudioPlayer.player.start(audioStream);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    private void playFlagLimitSound() {
        if(soundsRadio[0].isSelected()) {
            try {
                InputStream inputStream = getClass().getResourceAsStream("/sounds/no.wav");
                AudioStream audioStream = new AudioStream(inputStream);
                AudioPlayer.player.start(audioStream);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }    
    
    private void playClickSound() {
        if(soundsRadio[0].isSelected()) {
            try {
                InputStream inputStream = getClass().getResourceAsStream("/sounds/click.wav");
                AudioStream audioStream = new AudioStream(inputStream);
                AudioPlayer.player.start(audioStream);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }  
    
    private void playBalls() {
        if(soundsRadio[0].isSelected()) {
            try {
                InputStream inputStream = getClass().getResourceAsStream("/sounds/balls.wav");
                AudioStream audioStream = new AudioStream(inputStream);
                AudioPlayer.player.start(audioStream);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }     
    
    private void openBoard(boolean[][] hasMine) {  // open board after loss/win
        for(int i = 1; i < gridLength+1; i++) {
            for(int j = 1; j < gridLength+1; j++) {
                if((hasMine[i][j]) && (gButtons[i][j].getIcon() == null)) {                   
                    if(diffRadio[0].isSelected() || diffRadio[1].isSelected()) {
                        gButtons[i][j].setIcon(new ImageIcon(getClass().getResource("/images/bomb10.png")));
                    } else if (diffRadio[2].isSelected()) {
                        gButtons[i][j].setIcon(new ImageIcon(getClass().getResource("/images/bomb60.png"))); 
                    }
                } else if (gButtons[i][j].getIcon() == null){
                    gButtons[i][j].setEnabled(false);
                }
            }
        }
    }
    
    private void checkWin(boolean[][] hm) {
        for(int i = 1; i < gridLength+1; i++) {
            for(int j = 1; j < gridLength+1; j++) {
                if ((gButtons[i][j].isEnabled()) && ("".equals(gButtons[i][j].getText())) && (hm[i][j] == false)) {
                    return;    
                } 
            }           
        } endGameWin(hm);
    }
    
    private void endGameLoss(boolean[][] hm, int i, int j) {  // control bomb hits
        gButtons[i][j].removeMouseListener(this);
        if(diffRadio[0].isSelected() || diffRadio[1].isSelected()) {
            gButtons[i][j].setIcon(new ImageIcon(getClass().getResource("/images/bombHit10.png")));
        } else if (diffRadio[2].isSelected()) {
            gButtons[i][j].setIcon(new ImageIcon(getClass().getResource("/images/bombHit60.png"))); 
        }
        firstClick = true; // stops timer thread do/while
        openBoard(hm);
        playLoseSound();
        JOptionPane.showMessageDialog(null, "You Lose!", "Loser!", JOptionPane.INFORMATION_MESSAGE, (new ImageIcon(getClass().getResource("/images/bombHit10.png"))));
        resetGame();           
    }    
    
    private void endGameWin(boolean[][] hm) {
        firstClick = true; // stops timer thread do/while
        openBoard(hm);
        playWinSound();
        JOptionPane.showMessageDialog(null, "You Win!", "Winner!", JOptionPane.INFORMATION_MESSAGE, (new ImageIcon(getClass().getResource("/images/flag10.png")))); 
        resetGame();     
    }

    private void resetGame() {  // game reset control
        firstClick = true; // stops timer thread do/while
        gP1.removeAll();
        gP2.removeAll();
        setMineLocations();
        createHUD();
        createGameBoard();
        gP1.revalidate(); gP1.repaint();
        gP2.revalidate();gP2.repaint();
    }
    
    private void makeTimer() {
        Thread Timer = new Timer();
        Timer.start();
        firstClick = false;
    }
    
    public static void main(String[] args) {

    try {  // sets look and feel to windows for desired tabbed pane look
      UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
    } catch (Exception e) {
      e.printStackTrace();
    }    
        new MineSweeper("Mine Sweeper");
    }

    @Override
    public void mouseClicked(MouseEvent e) {        
        if(e.getSource() == newGame) {
            resetGame();
        }
        
        if(e.getSource() == diffRadio[0]) {
            resetGame();
        } else if(e.getSource() == diffRadio[1]) {
            resetGame();
        } else if(e.getSource() == diffRadio[2]) {
            playBalls();
            resetGame();
        }               
    }

    @Override
    public void mousePressed(MouseEvent e) {     
        if(SwingUtilities.isLeftMouseButton(e)) {
        
            for (int i = 0; i < gridLength+1; i++) {
                for (int j = 0; j < gridLength+1; j++) {
                    if(e.getSource() == gButtons[i][j]) {
                    if (firstClick) {  // start timer on first click
                        makeTimer();
                    }
                        playClickSound();
                        if(diffRadio[0].isSelected()) {
                            clickLogic(hasMineE, i, j);
                        } else if (diffRadio[1].isSelected()){
                            clickLogic(hasMineN, i, j);
                        } else if (diffRadio[2].isSelected()) {
                            clickLogic(hasMineH, i, j);
                        }        
                    }   
                }
            }
        } else if (SwingUtilities.isRightMouseButton(e)) {
            for (int i = 1; i < gridLength+1; i++) {
                for (int j = 1; j < gridLength+1; j++) {
                    if(e.getSource() == gButtons[i][j]) {
                        flagToggle(i,j);
                    }
                }
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
 
    @Override
    public void mouseExited(MouseEvent e) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}