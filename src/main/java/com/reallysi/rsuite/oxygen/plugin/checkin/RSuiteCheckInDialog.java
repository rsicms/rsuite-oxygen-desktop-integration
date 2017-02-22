package com.reallysi.rsuite.oxygen.plugin.checkin;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import com.reallysi.rsuite.api.VersionType;
import com.reallysi.rsuite.client.api.impl.RsuiteRepositoryImpl;

/**
 * A simple dialog, similar to check in found in RSuite
 * performs checkin of document to rsuite
 */
public class RSuiteCheckInDialog extends JDialog{

    private static final long serialVersionUID = 1L;
    
    private boolean isFileCheckedIn = false;

    public RSuiteCheckInDialog(
            final RsuiteRepositoryImpl repository,
            final String moId
    ){
        super();
        setModal(true);
        setTitle("RSuite Check In");
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        GridBagLayout layout = new GridBagLayout();
        getContentPane().setLayout(layout);

        // set up components
        String[] versions = {"Major", "Minor"};
        final JComboBox versionTypeBox = new JComboBox(versions);

        final JTextArea versionNote = new JTextArea("", 4, 40);
        versionNote.setLineWrap(true);
        versionNote.setWrapStyleWord(true);
        
        final JLabel saveLabel = new JLabel("<html><b>Be sure to save any changes with File>Save before checking in!</b></html>");
        saveLabel.setOpaque(false);
        
        JButton okButton = new JButton("Check In");
        JButton cancelButton = new JButton("Cancel");
        
        JLabel versionTypeLabel = new JLabel("Version");
        JLabel versionNoteLabel = new JLabel("Version Note");

        // Set up constraints
        GridBagConstraints c = new GridBagConstraints();
        c.insets.top = 5;
        c.insets.bottom = 5;
        c.insets.right = 4;
        JPanel pane = new JPanel(layout);
        pane.setBorder(BorderFactory.createEmptyBorder(10, 20, 5, 20));
        c.anchor = GridBagConstraints.NORTHWEST;

        c.gridwidth = 2;
        layout.setConstraints(saveLabel, c);
        pane.add(saveLabel);

        c.gridy = 1;
        c.gridwidth = 1;
        layout.setConstraints(versionTypeLabel, c);
        pane.add(versionTypeLabel);
        layout.setConstraints(versionTypeBox, c);
        pane.add(versionTypeBox);

        c.gridy = 2;
        layout.setConstraints(versionNoteLabel, c);
        pane.add(versionNoteLabel);
        layout.setConstraints(versionNote, c);
        pane.add(versionNote);

        c.gridy = 3;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.anchor = GridBagConstraints.CENTER;
        JPanel panel = new JPanel();
        panel.add(okButton);
        panel.add(cancelButton);
        layout.setConstraints(panel, c);
        pane.add(panel);

        // add to pane
        getContentPane().add(pane);

        // adjust size
        pack();

        // add action listener to ok button
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){

                java.io.PrintStream out =
                    OxyUtils.openLogFile("errorlog_handler_checkindialog.txt");

                ClassLoader saved = Thread.currentThread().getContextClassLoader();
                try {
                    Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());

                    out.println("CHECKING IN " + moId + "...");
                    int versionType = (versionTypeBox.getSelectedIndex() == 0) ? VersionType.MAJOR.value() : VersionType.MINOR.value();
                    repository.checkIn(moId, versionNote.getText(), versionType);
                    isFileCheckedIn = true;
                    out.println("CHECK IN COMPLETE.");

                    Frame frame = new Frame();
                    JOptionPane.showMessageDialog(frame, "RSuite Check In Complete", "Success", JOptionPane.INFORMATION_MESSAGE);

                    // document close will go here, not possible as of 10/07/09
                    // using Oxygen 11 beta
                }
                catch(Throwable t){
                    out.println("CAUGHT EXCEPTION, RAISING ERROR DIALOG...");
                    t.printStackTrace(out);
                    try {
                        Frame frame = new Frame();
                        JOptionPane.showMessageDialog(frame,
                                "Error: "+t.getLocalizedMessage(),
                                "Cannot Check In Document",
                                JOptionPane.ERROR_MESSAGE);
                    } catch (Throwable t2) {
                        out.println("RAISING ERROR DIALOG EXCEPTION:");
                        t2.printStackTrace(out);
                    }
                }
                finally{
                    Thread.currentThread().setContextClassLoader(saved);
                    out.flush();
                    out.close();
                }

                setVisible(false);
                dispose();
            }
        });

        // add action listener to cancel button
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                setVisible(false);
            }
        });
    }

    public boolean isFileCheckedIn () {
    	return isFileCheckedIn;
    }
}
