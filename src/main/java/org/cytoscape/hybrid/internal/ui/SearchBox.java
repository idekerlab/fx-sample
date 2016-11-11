package org.cytoscape.hybrid.internal.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolTip;
import javax.swing.ToolTipManager;
import javax.swing.border.EmptyBorder;

import org.cytoscape.hybrid.events.InterAppMessage;
import org.cytoscape.hybrid.internal.ws.ExternalAppManager;
import org.cytoscape.hybrid.internal.ws.WSClient;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class SearchBox extends JPanel {

	private static final long serialVersionUID = 5216512744558942600L;

	private static final Logger LOGGER = LoggerFactory.getLogger(SearchBox.class);
	
	private static final Icon NDEX_LOGO = new ImageIcon(
			SearchBox.class.getClassLoader().getResource("images/ndex-logo.png"));

	private static final Icon ICON_SEARCH = new ImageIcon(
			SearchBox.class.getClassLoader().getResource("images/search.png"));
	private static final Icon ICON_SETTINGS = new ImageIcon(
			SearchBox.class.getClassLoader().getResource("images/settings.png"));

	private static final Dimension PANEL_SIZE = new Dimension(400, 40);
	private static final Dimension PANEL_SIZE_MAX = new Dimension(900, 100);
	private static final Dimension TEXT_AREA_SIZE = new Dimension(250, 24);

	private static final Dimension BUTTON_SIZE = new Dimension(30, 30);
	
	private static final Font SEARCH_TEXT_FONT = new Font("SansSerif", Font.BOLD, 11);
	private static final Font SEARCH_TEXT_FONT2 = new Font("SansSerif", Font.PLAIN, 12);
	private static final Color TEXT_COLOR  = Color.decode("#444444");

	private static final String PLACEHOLDER = "Enter search terms for NDEx...";

	private final JLabel iconLabel;
	private final JTextField searchTextField;
	private final JPanel searchButton;
	private final JPanel settingButton;

	// WS Client
	private final WSClient client;
	private final ObjectMapper mapper;

	// States
	private Boolean searchBoxClicked = false;

	// For child process management
	private final ExternalAppManager pm;

	private final String command;

	public SearchBox(final WSClient client, final ExternalAppManager pm, String command) {

		this.mapper = new ObjectMapper();

		this.client = client;
		this.pm = pm;
		this.command = command;

		this.setPreferredSize(PANEL_SIZE);
		this.setSize(PANEL_SIZE);
		this.setMaximumSize(PANEL_SIZE_MAX);

		this.iconLabel = new JLabel();
		this.iconLabel.setBorder(new EmptyBorder(3,5,3,5));
		this.iconLabel.setBackground(Color.WHITE);
		this.iconLabel.setSize(BUTTON_SIZE);
		this.iconLabel.setIcon(NDEX_LOGO);

		this.searchTextField = new JTextField() {
			@Override
			public JToolTip createToolTip() {
				final Dimension size = new Dimension(220, 270);
				final JEditorPane pane = new JEditorPane();
				pane.setPreferredSize(size);
				pane.setBackground(Color.white);
				pane.setEditable(false);
				pane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
				pane.setFont(SEARCH_TEXT_FONT2);
				pane.setForeground(TEXT_COLOR);
				pane.setContentType("text/html");
				final String help = "<h3>NDEx Database Search</h3>"
						+ "<p>Enter search query for NDEx database. You can use</p>"
						+ "<br/>  - Gene names<br/>  - Gene IDs<br/>  - Keywords<br/>  - etc.<br/>"
						+ "<p>If you want to browse the database, simply send empty query. For more details, please visit <i>www.ndexbio.org</i></p>";
				pane.setText(help);
				pane.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

				ToolTipManager.sharedInstance().setDismissDelay(10000);
				JToolTip tip = new JToolTip();
				tip.setPreferredSize(size);
				tip.setSize(size);
				tip.setComponent(this);
				tip.setLayout(new BorderLayout());
				tip.add(pane, BorderLayout.CENTER);
				return tip;
			}
		};
		this.searchTextField.setToolTipText("");
	
		this.searchTextField.setBorder(null);
		this.searchTextField.setFont(SEARCH_TEXT_FONT);
		this.searchTextField.setBackground(Color.white);
		this.searchTextField.setForeground(Color.decode("#333333"));
		this.searchTextField.setPreferredSize(TEXT_AREA_SIZE);
		this.searchTextField.setSize(TEXT_AREA_SIZE);
		this.searchTextField.setText(PLACEHOLDER);
		this.searchTextField.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				processClick();
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
				if (!searchBoxClicked) {
					searchTextField.setText("");
					searchBoxClicked = true;
				}
			}
			
			private final void processClick() {
				if (!isEnabled()) {
					MessageUtil.reauestExternalAppFocus(client);
					return;
				}

				if (!searchBoxClicked) {
					searchTextField.setText("");
					searchBoxClicked = true;
				}
			}
		});
		this.searchTextField.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					search(searchTextField.getText());
				} catch (Exception e1) {
					e1.printStackTrace();
					LOGGER.error("Could not finish search.", e1);
				}
			}
		});

		this.searchButton = new JPanel();
		this.settingButton = new JPanel();
		this.searchButton.setLayout(new BorderLayout());
		this.settingButton.setLayout(new BorderLayout());
		this.searchButton.setBorder(new EmptyBorder(3, 5, 3, 5));
		this.settingButton.setBorder(new EmptyBorder(3, 5, 3, 5));
		this.searchButton.setBackground(Color.WHITE);
		this.settingButton.setBackground(Color.WHITE);
		
		final JLabel searchIconLabel = new JLabel(ICON_SEARCH);
		final JLabel settingIconLabel = new JLabel(ICON_SETTINGS);
		searchIconLabel.setOpaque(false);
		settingIconLabel.setOpaque(false);
		this.searchButton.add(searchIconLabel, BorderLayout.CENTER);
		this.settingButton.add(settingIconLabel, BorderLayout.CENTER);
		
		this.settingButton.setSize(BUTTON_SIZE);
		this.searchButton.setSize(BUTTON_SIZE);

		this.searchButton.setBackground(Color.white);
		this.settingButton.setBackground(Color.white);

		this.searchButton.setToolTipText("Start NDEx search (opens new window)");
		this.settingButton.setToolTipText("Open settings");
		
		this.searchButton.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mouseClicked(MouseEvent e) {
				System.out.println("Search start!----------");
				try {
					search(searchTextField.getText());
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		
		this.settingButton.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mouseClicked(MouseEvent e) {
				try {
					setting();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});

		final JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(1, 2));
		buttonPanel.add(searchButton);
		buttonPanel.add(settingButton);

		setLayout(new BorderLayout());
		add(iconLabel, BorderLayout.WEST);
		add(searchTextField, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.EAST);
		
		this.setOpaque(true);
		this.setBackground(Color.WHITE);
		this.setBorder(new EmptyBorder(3,3,3,3));
	}

	
	private final void search(String query) throws Exception {
		// Use empty String if default text is used.
		if (query.equals(PLACEHOLDER)) {
			query = "";
		}

		pm.setQuery(query);
		
		execute("ndex");
	}	

	private final void setting() {
		try {
			execute("ndex-login");
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	private final void execute(final String app) throws Exception {

		final String dest = "ws://localhost:8025/ws/echo";
		client.start(dest);

		if (pm.isActive()) {
			final InterAppMessage focus = InterAppMessage.create().setFrom(InterAppMessage.FROM_CY3)
					.setType(InterAppMessage.TYPE_FOCUS);
			this.client.getSocket().sendMessage(mapper.writeValueAsString(focus));
			return;
		}

		final ExecutorService executor = Executors.newSingleThreadExecutor();
		executor.submit(() -> {
			try {
				// Set application type:
				this.client.getSocket().setApplication(app);
				System.out.println("Command: " + command);
				pm.setProcess(Runtime.getRuntime().exec(command));
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}
	
}
