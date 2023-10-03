package org.example;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

class Labels {
    public static void main(String[] args) {
        LabelFrame frame = new LabelFrame();
        frame.setSize(150, 200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}

class LabelFrame extends JFrame{
    //створюємо панель
    JPanel panel=new JPanel();

    LabelFrame(){
        // вирівнювання за замовчуванням (CENTER)
        JLabel label1 = new JLabel("Багато левів, ");
        // вирівнювання вліво
        JLabel label2 = new JLabel("тигрів з тигрицями", SwingConstants.LEFT);
        // мітка без тексту, вирівнювання за замовчуванням
        JLabel label3 = new JLabel();
        // створюємо іконку
        Icon icon = new ImageIcon("icon.gif");
        // створюємо мітку із зображенням
        JLabel label4 = new JLabel(icon);
        // задаємо текст для label3
        label3.setText("і ведмедів");
        // встановлюємо вирівнювання
        label3.setHorizontalAlignment(SwingConstants.RIGHT);
        //додаємо мітки в панель
        panel.add(label1);
        panel.add(label2);
        panel.add(label3);
        panel.add(label4);
        //додаємо панель у фрейм
        this.add(panel);
    }
}