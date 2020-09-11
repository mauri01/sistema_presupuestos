package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {

		JFrame frame = new JFrame("Sistema de Ventas");
		frame.setSize(300, 150);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);

		JPanel panel = new JPanel();
		frame.add(panel);
		placeComponents(panel);

		frame.setVisible(true);

		try{
			SpringApplication.run(DemoApplication.class, args);
			JButton loginButton = new JButton("Ingresar");
			loginButton.setBounds(60, 80, 150, 25);
			panel.add(loginButton);

			loginButton.addActionListener(new ActionListener() {
									   public void actionPerformed(ActionEvent e) {
										   try {
											   Desktop.getDesktop().browse(URI.create("http://localhost:8284/"));
										   } catch (IOException e1) {
											   e1.printStackTrace();
										   }
									   }
								   });

			panel.updateUI();
		}catch (Exception e){

			JLabel userLabel1 = new JLabel("Configuacion incorrecta.");
			userLabel1.setBounds(10, 50, 260, 25);
			userLabel1.setForeground(Color.RED);
			panel.add(userLabel1);

			JLabel userLabel11 = new JLabel("Revise XAMPP y Base de Datos");
			userLabel11.setBounds(10, 70, 260, 25);
			userLabel11.setForeground(Color.RED);
			panel.add(userLabel11);

			JLabel userLabel11s = new JLabel(e.getMessage());
			userLabel11s.setBounds(10, 90, 260, 25);
			userLabel11s.setForeground(Color.RED);
			panel.add(userLabel11s);

			panel.updateUI();
		}

	}

	private static void placeComponents(JPanel panel) {

		panel.setLayout(null);

		JLabel userLabel = new JLabel("El programa se esta iniciando...");
		userLabel.setBounds(10, 10, 260, 25);
		panel.add(userLabel);

		JLabel userLabel1 = new JLabel("Aguarde un momento");
		userLabel1.setBounds(10, 30, 260, 25);
		panel.add(userLabel1);
	}
}
