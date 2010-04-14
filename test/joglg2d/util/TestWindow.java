package joglg2d.util;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import joglg2d.JOGLPanel;

/**
 * @author borkholder
 * @created Feb 6, 2010
 *
 */
@SuppressWarnings("serial")
public class TestWindow extends JFrame {
  public static final int SAME = 0;

  public static final int DIFFERENT = 1;

  private Painter painter;

  private int result = -1;

  public TestWindow() {
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

    initialize();
  }

  private void initialize() {
    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    JComponent java2d = new JPanel() {
      @Override
      public void paint(Graphics g) {
        if (painter != null) {
          painter.paint((Graphics2D) g);
        }
      }
    };

    JComponent jogl = new JOGLPanel() {
      @Override
      public void paint(Graphics g) {
        if (painter != null) {
          painter.paint((Graphics2D) g);
        }
      }
    };

    splitPane.setLeftComponent(jogl);
//    splitPane.setRightComponent(java2d);
    splitPane.setResizeWeight(0.5);

    JButton sameButton = new JButton("Same");
    sameButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        result = SAME;
      }
    });

    JButton differentButton = new JButton("Different");
    differentButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        result = DIFFERENT;
      }
    });

    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    buttonPanel.add(sameButton);
    buttonPanel.add(differentButton);

    setLayout(new BorderLayout());
    add(splitPane, BorderLayout.CENTER);
    add(buttonPanel, BorderLayout.SOUTH);

    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        result = DIFFERENT;
      }
    });
  }

  public void setPainter(Painter painter) {
    this.painter = painter;
  }

  public int waitForInput() throws InterruptedException {
    while (result == -1) {
      Thread.sleep(100);
    }

    int value = result;
    result = -1;
    return value;
  }
}
