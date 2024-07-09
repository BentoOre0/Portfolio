

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import javax.swing.table.TableColumn;
import raven.cell.TableActionCellEditor;
import raven.cell.TableActionCellRender;
import raven.cell.TableActionEvent;
/**
 *
 * @author jerem
 */

public class Home extends javax.swing.JFrame {
    
    //Define some checking functions
    public static boolean ASCIIandAlNumonly(String input) // check if only alphanumeric plus keyboard chars only
    {
        int a;
        for (int i = 0; i < input.length(); i++) 
        {
            if (Character.isDigit(input.charAt(i))){
                //numbers allowed
            } else {
                try{
                    a = (int)input.charAt(i);//change to ascii value, if error then return false
                } catch (Exception e2){
                    return false;
                }
                
            }
        }
        return true;
    }
    
    
    public static int charcount(String charlist, char elem) {
        String reduced = charlist;
        reduced = reduced.replace( String.valueOf(elem),"");
        return charlist.length()-reduced.length();
    }

    public static int[] occur(String charlist, char elem) {
        // used to get indexes of all periods. I will use to make sure that each period has atleast 2 characters between them
        int j = 0;
        String temp = "";
        for (int i = 0; i < charlist.length(); i++) {
            if (charlist.charAt(i) == elem) {
                temp += String.valueOf(i);
                temp += " ";
            }
        }
        String[] lines = temp.split(" ");
        int[] myindexes = new int[lines.length];
        for (int i = 0; i < myindexes.length; i++) {
            myindexes[i] = Integer.parseInt(lines[i]);
        }
        return myindexes;
    }

    public static boolean check_special_char(String charlist) {
        int asciival;
        for (int i = 0; i < charlist.length(); i++) {
            asciival = (int) charlist.charAt(i);
            if ((asciival >= 32 && asciival <= 47 && asciival != 46) || (asciival >= 58 && asciival <= 63 && asciival != 64) || (asciival >= 91 && asciival <= 96 && asciival != 95) || (asciival >= 123 && asciival <= 126)) {
                return true;// has a special character init with some exceptions ('@','.','_')
            }
        }
        return false;
    }
    public static boolean checkemail(String email) {
        if (!ASCIIandAlNumonly(email)){
            return false;
        } else if (charcount(email, '@') != 1) {
            return false;
        } else if (charcount(email.substring(email.lastIndexOf('@') + 1), '.') < 1) {
            return false;
        } else if (email.substring(email.lastIndexOf('@'), email.lastIndexOf('.')).length() < 2) {
            return false;
        } else if (email.substring(0, email.lastIndexOf('@')).length() < 3) {
            return false;
        } else if (charcount(email, ' ') > 0) {
            return false;
        } else if (check_special_char(email)) {
            return false;
        }
        int[] list = occur(email, '.');
        String sub;
        
        // make sure there is atleast 2 characters between each period
        for(int i = 0; i < list.length-1; i++) {
            sub = email.substring(list[i], list[i + 1]);
            if(sub.length() < 2){
                return false;
            }
        }
        return true;
    }
    
    
    
    // Will double check this later
    public static boolean doesTableExists(String tableName, Connection con) throws SQLException {
        DatabaseMetaData meta = con.getMetaData();
        ResultSet result = meta.getTables(null, null, tableName.toUpperCase(), null);
       
        return result.next();
    }
    
    /**
     * Creates new form NewJFrame
     */
    public Home() {
        initComponents();
        TableActionEvent event = new TableActionEvent() {
            @Override
            public void onEdit(int row){
                try {
                    DefaultTableModel model = (DefaultTableModel) tblClothes.getModel();
//                    int row = tblClothes.getSelectedRow();
                    txtName.setText(model.getValueAt(row, 1).toString());
                    txtEmail.setText(model.getValueAt(row, 2).toString());
                    cmbItem.setSelectedItem(model.getValueAt(row, 3));
                    txtSize.setText(model.getValueAt(row, 4).toString());
                    txtCost.setText(model.getValueAt(row, 5).toString());
                    cmbStatus.setSelectedItem(model.getValueAt(row, 6));
                    tarNotes.setText(model.getValueAt(row, 8).toString());
                    txtCommission.setText(model.getValueAt(row,7).toString());
                    
                } catch (Exception E) {

                }
            }
            @Override
            public void onDelete(int row){
                if (tblClothes.isEditing()) {
                    tblClothes.getCellEditor().stopCellEditing();
                }
                try {
                    DefaultTableModel model = (DefaultTableModel) tblClothes.getModel();
                    int rownum = tblClothes.getSelectedRow();
                    if (rownum == -1) {
                        JOptionPane.showMessageDialog(null, "Please select a row");
                    } else {
                        int idremov = Integer.parseInt(model.getValueAt(rownum, 0).toString());
                        String databaseURL2 = "jdbc:derby:inventory";
                        Connection con2 = DriverManager.getConnection(databaseURL2);
                        Statement st2 = con2.createStatement();

                        //original QUERY
                        String sql2 = "DELETE FROM clothes WHERE sid = " + idremov + "";//                            String sql1="INSERT INTO students(sid, name, age, email) VALUES('"+txtId.getText()+"','"+txtName.getText()+"','"+cmbAge.getSelectedItem().toString()+"','"+txtEmail.getText()+"')";
                        st2.executeUpdate(sql2);
                        con2.close();
                        
                        
                        
                        String databaseURL = "jdbc:derby:inventory";
                        Connection con = DriverManager.getConnection(databaseURL);
                        Statement st = con.createStatement();
                        String sql = "select * from clothes";
                        ResultSet rs = st.executeQuery(sql);

                        model.setRowCount(0);// clearing the table
                        ArrayList<String> status = new ArrayList<String>();
                        while (rs.next() == true) // does something exist in the resultset
                        {
                            status.add(rs.getString(7));
                            model.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getDouble(6), rs.getString(7), rs.getDouble(8), rs.getString(9), rs.getString(10)}); // not 0 indexed find id, name, age and then email
                        }
                        int countAvailable = Collections.frequency(status, "AVAILABLE");
                        int countReserved = Collections.frequency(status, "RESERVED");
                        int countDamaged = Collections.frequency(status, "DAMAGED");
                        int countSold = Collections.frequency(status, "SOLD");
//                        tblClothes.getColumnModel().getColumn(10).setCellRenderer(new TableActionCellRender());/
                        con.close();
                    }
                } catch (Exception E) {

                }                
                
            }
            @Override
            public void onView(int row){
                try{
                    //might use in future
                    String databaseURL2 = "jdbc:derby:orders";
                    Connection con2 = DriverManager.getConnection(databaseURL2);
                    Statement st2 = con2.createStatement();
                    DefaultTableModel model2 = (DefaultTableModel) tblClothes.getModel();
                    int rownum2 = tblClothes.getSelectedRow();
                    int idview2 = Integer.parseInt(model2.getValueAt(rownum2, 0).toString());
                    String sql2 = "select sum(profit) from Transactions where prodid = " + idview2 + "";
                    ResultSet rs2 = st2.executeQuery(sql2);
                    double total = 0.0;
                    while (rs2.next()){
                        total = rs2.getDouble(1);
                    }
                    
                    String databaseURL1 = "jdbc:derby:orders";
                    Connection con1 = DriverManager.getConnection(databaseURL1);
                    Statement st1 = con1.createStatement();
                    DefaultTableModel model = (DefaultTableModel) tblClothes.getModel();
                    int rownum = tblClothes.getSelectedRow();
                    int idview = Integer.parseInt(model.getValueAt(rownum, 0).toString());
                    String sql1 = "select date,email,pemail from Transactions where prodid = "+idview+"";
                    ResultSet rs = st1.executeQuery(sql1);
                    String out = "Total Profit: "+total+"\nAll Transactions So Far:\nDate | Current Owner | Previous Owner\n";
                    while (rs.next()){
                        out += rs.getString(1);
                        out += " | ";
                        out += rs.getString(2);
                        out += " | ";
                        out += rs.getString(3);
                        out += "\n";
                    }
                    JOptionPane.showMessageDialog(null, out);
                } catch (Exception E){
                    
                }
            } 
        };
        tblClothes.getColumnModel().getColumn(10).setCellRenderer(new TableActionCellRender());
        tblClothes.getColumnModel().getColumn(10).setCellEditor(new TableActionCellEditor(event));
        try{
            
            String databaseURL = "jdbc:derby:inventory;create=true";
            Connection con = DriverManager.getConnection(databaseURL);
            Statement st = con.createStatement();
            //errors
            
            if (!doesTableExists("clothes", con)) {
                //create table and fields
                String sql = "create table clothes (sid int, name varchar(256), email varchar(100), item varchar(256), size varchar(256), cost double, status varchar(256), commission double, notes varchar(256), date varchar(256))";
                
                
                st.execute(sql);
            }

            
            con.close();
            DefaultTableModel model = (DefaultTableModel) tblClothes.getModel();

            String databaseURL1 = "jdbc:derby:inventory";
            Connection con1 = DriverManager.getConnection(databaseURL1);
            Statement st1 = con1.createStatement();
            String sql1 = "select * from clothes";
            ResultSet rs = st1.executeQuery(sql1);

            model.setRowCount(0);// clearing the table
            ArrayList<String> status = new ArrayList<String>();
            while (rs.next() == true) // does something exist in the resultset
            {
                status.add(rs.getString(7));
                model.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getDouble(6), rs.getString(7), rs.getDouble(8), rs.getString(9), rs.getString(10)}); // not 0 indexed find id, name, age and then email
            }
            int countAvailable = Collections.frequency(status, "AVAILABLE");
            int countReserved = Collections.frequency(status, "RESERVED");
            int countDamaged = Collections.frequency(status, "DAMAGED");
            int countSold = Collections.frequency(status, "SOLD");
//                        tblClothes.getColumnModel().getColumn(10).setCellRenderer(new TableActionCellRender());/
            con.close();
            //
        } catch (Exception e){

        }

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        txtEmail = new javax.swing.JTextField();
        txtName = new javax.swing.JTextField();
        txtCost = new javax.swing.JTextField();
        btnSave = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        btnClear = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        btnUpdate = new javax.swing.JButton();
        txtSearch = new javax.swing.JTextField();
        btnSearch = new javax.swing.JButton();
        txtSize = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        cmbItem = new javax.swing.JComboBox<>();
        jScrollPane2 = new javax.swing.JScrollPane();
        tarNotes = new javax.swing.JTextArea();
        jLabel7 = new javax.swing.JLabel();
        btnReset = new javax.swing.JButton();
        cmbStatus = new javax.swing.JComboBox<>();
        jLabel8 = new javax.swing.JLabel();
        txtCommission = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblClothes = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        jPanel1.setBackground(new java.awt.Color(204, 204, 204));

        jPanel2.setBackground(new java.awt.Color(204, 204, 204));

        jPanel3.setBackground(new java.awt.Color(102, 153, 255));

        txtEmail.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtEmailActionPerformed(evt);
            }
        });

        txtName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtNameActionPerformed(evt);
            }
        });

        txtCost.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtCostActionPerformed(evt);
            }
        });

        btnSave.setText("Save");
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });

        jLabel1.setForeground(new java.awt.Color(0, 0, 0));
        jLabel1.setText("Commission");

        btnClear.setText("Clear");
        btnClear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClearActionPerformed(evt);
            }
        });

        jLabel2.setForeground(new java.awt.Color(0, 0, 0));
        jLabel2.setText("Name");

        jLabel3.setForeground(new java.awt.Color(0, 0, 0));
        jLabel3.setText("Size");

        jLabel4.setForeground(new java.awt.Color(0, 0, 0));
        jLabel4.setText("Email");

        jLabel5.setForeground(new java.awt.Color(0, 0, 0));
        jLabel5.setText("Cost");

        btnUpdate.setText("Update");
        btnUpdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpdateActionPerformed(evt);
            }
        });

        txtSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtSearchActionPerformed(evt);
            }
        });

        btnSearch.setText("Search");
        btnSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchActionPerformed(evt);
            }
        });

        jLabel6.setForeground(new java.awt.Color(0, 0, 0));
        jLabel6.setText("Item");

        cmbItem.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "-SELECT-", "SENIOR POLO SHIRT MALE", "SENIOR PANTS MALE", "SENIOR SHORTS MALE", "SENIOR BLOUSE FEMALE", "SENIOR SKIRT FEMALE", "SENIOR SKORT FEMALE", "SENIOR PANTS FEMALE", "IB POLO SHIRT MALE", "IB PANTS MALE", "IB SHORTS MALE", "IB BLOUSE FEMALE", "IB SKIRT FEMALE", "IB SKORT FEMALE", "IB PANTS FEMALE" }));
        cmbItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbItemActionPerformed(evt);
            }
        });

        tarNotes.setColumns(20);
        tarNotes.setRows(5);
        jScrollPane2.setViewportView(tarNotes);

        jLabel7.setForeground(new java.awt.Color(0, 0, 0));
        jLabel7.setText("Notes");

        btnReset.setText("Reset");
        btnReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnResetActionPerformed(evt);
            }
        });

        cmbStatus.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "-SELECT-", "AVAILABLE", "RESERVED", "DAMAGED", "SOLD" }));

        jLabel8.setForeground(new java.awt.Color(0, 0, 0));
        jLabel8.setText("Status");

        txtCommission.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtCommissionActionPerformed(evt);
            }
        });

        jLabel9.setForeground(new java.awt.Color(0, 0, 0));
        jLabel9.setText("RETURN OR ADD ITEM");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(73, 73, 73)
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(105, 105, 105)
                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(110, 110, 110)
                        .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(139, 139, 139)
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(83, Short.MAX_VALUE))
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(txtCost, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 99, Short.MAX_VALUE)
                                    .addComponent(txtEmail, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtSize, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtCommission))
                                .addGap(18, 18, 18))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addGap(35, 35, 35)))
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                            .addComponent(cmbStatus, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(cmbItem, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                                .addContainerGap(80, Short.MAX_VALUE)
                                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(53, 53, 53))
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGap(73, 73, 73)
                                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(54, 54, 54)))
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGap(50, 50, 50)
                                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(42, 42, 42))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                                .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(64, 64, 64)))))
                .addContainerGap(13, Short.MAX_VALUE))
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(56, 56, 56)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(btnSave, javax.swing.GroupLayout.DEFAULT_SIZE, 99, Short.MAX_VALUE)
                            .addComponent(btnUpdate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnClear, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(btnReset, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnSearch, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbItem, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jLabel8))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtEmail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbStatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jLabel7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(txtCost, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtCommission, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 114, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSave)
                    .addComponent(btnClear))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnUpdate)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSearch)
                    .addComponent(jLabel9))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnReset)
                .addGap(114, 114, 114))
        );

        tblClothes.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Name", "Email", "Item", "Size", "Buy Back", "Status", "Commission", "Notes", "Date", "Action"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false, false, true
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblClothes.setRowHeight(40);
        tblClothes.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tblClothes.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblClothesMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblClothes);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 1118, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 531, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 554, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>                        

    private void txtEmailActionPerformed(java.awt.event.ActionEvent evt) {                                         
        // TODO add your handling code here:
    }                                        

    private void txtCostActionPerformed(java.awt.event.ActionEvent evt) {                                        
        // TODO add your handling code here:
    }                                       

    private void txtNameActionPerformed(java.awt.event.ActionEvent evt) {                                        
        // TODO add your handling code here:
    }                                       

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {                                        
        // TODO add your handling code here:
        try {
            //if has inside
            if (txtName.getText() == "" || txtEmail.getText() == "" || cmbItem.getSelectedIndex() == 0 || txtSize.getText() == "" || txtCost.getText() == "" ||cmbStatus.getSelectedIndex() == 0|| txtCommission.getText() == "") {
                JOptionPane.showMessageDialog(null, "PLEASE FILL IN THE FIELDS");
            } else {
                String Email = txtEmail.getText();
                if (checkemail(Email) == false) {
                    JOptionPane.showMessageDialog(null, "PLEASE FILL IN A VALID EMAIL FORMAT");
                } else {
                    // check if valid cost
                    try {
                        double cost = Double.parseDouble(txtCost.getText());
                        // error will return if cost is not reasonable

                        // add to DB
                        int max = 0;
                        String databaseURL2 = "jdbc:derby:inventory";
                        Connection con2 = DriverManager.getConnection(databaseURL2);

                        Statement st2 = con2.createStatement();

                        String sql2 = "select sid from clothes";
                        ResultSet rs2 = st2.executeQuery(sql2);

                        while (rs2.next() == true) // does something exist in the resultset
                        {
                            if (rs2.getInt(1) > max) {
                                max = rs2.getInt(1);
                            }

                        }

                        con2.close();
                        max++; // get maximum id and increment to get current id
                        DefaultTableModel model = (DefaultTableModel) tblClothes.getModel();
                        
                        String databaseURL1 = "jdbc:derby:inventory";
                        
                        Connection con1 = DriverManager.getConnection(databaseURL1);
                        Statement st1 = con1.createStatement();

                        
                        LocalDate currentDate = LocalDate.now();
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                        String currentDateAsString = currentDate.format(formatter);
                        

                        String sql1 = "INSERT INTO clothes(sid,name,email,item,size,cost,status,commission, notes, date) VALUES (" + max + ",'" + txtName.getText() + "','"+txtEmail.getText()+"','" + cmbItem.getSelectedItem().toString() + "','" + txtSize.getText().toUpperCase() + "'," + Double.valueOf(txtCost.getText()) + ",'"+cmbStatus.getSelectedItem().toString()+"'," + Double.valueOf(txtCommission.getText()) + ",'"+tarNotes.getText()+"','"+currentDateAsString+"')";
                        
                        st1.execute(sql1);

                        con1.close();
                        String databaseURL = "jdbc:derby:inventory";
                        Connection con = DriverManager.getConnection(databaseURL);
                        Statement st = con.createStatement();
                        String sql = "select * from clothes";
                        ResultSet rs = st.executeQuery(sql);

                        model.setRowCount(0);// clearing the table
                        ArrayList<String> status = new ArrayList<String>();
                        while (rs.next() == true) // does something exist in the resultset
                        {
                            status.add(rs.getString(7));
                            model.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getDouble(6), rs.getString(7), rs.getDouble(8), rs.getString(9), rs.getString(10)}); // not 0 indexed find id, name, age and then email
                        }
                        int countAvailable = Collections.frequency(status, "AVAILABLE");
                        int countReserved = Collections.frequency(status, "RESERVED");
                        int countDamaged = Collections.frequency(status, "DAMAGED");
                        int countSold = Collections.frequency(status, "SOLD");
//                        tblClothes.getColumnModel().getColumn(10).setCellRenderer(new TableActionCellRender());/
                        con.close();
                        

                        //clear it all
                        txtName.setText("");
                        txtEmail.setText("");
                        txtCost.setText("");
                        txtSize.setText("");
                        tarNotes.setText("");
                        txtCommission.setText("");
                        cmbItem.setSelectedIndex(0);
                        cmbStatus.setSelectedIndex(0);
                        

                    } catch (Exception e) {

                        JOptionPane.showMessageDialog(null, "PLEASE FILL IN A VALID COST. TRY REMOVING SYMBOLS.");
                    }
                }
            }
        } catch (Exception E){

        }
    }                                       

    private void btnClearActionPerformed(java.awt.event.ActionEvent evt) {                                         
        // TODO add your handling code here:
        try{
            txtName.setText("");
            txtEmail.setText("");
            txtCost.setText("");
            txtSize.setText("");
            tarNotes.setText("");
            txtCommission.setText("");
            cmbItem.setSelectedIndex(0);
            cmbStatus.setSelectedIndex(0);
        } catch (Exception E){
            
        }
        
    }                                        

    private void btnUpdateActionPerformed(java.awt.event.ActionEvent evt) {                                          
        // TODO add your handling code here:
                try {
            //if has inside
            if (txtName.getText() == "" || txtEmail.getText() == "" || cmbItem.getSelectedIndex() == 0 || txtSize.getText() == "" || txtCost.getText() == "" ||cmbStatus.getSelectedIndex() == 0|| txtCommission.getText() == "") {
                JOptionPane.showMessageDialog(null, "PLEASE FILL IN THE FIELDS");
            } else {
                String Email = txtEmail.getText();
                if (checkemail(Email) == false) {
                    JOptionPane.showMessageDialog(null, "PLEASE FILL IN A VALID EMAIL FORMAT");
                } else {
                    // check if valid cost
                    try {
                        double cost = Double.parseDouble(txtCost.getText());
                        // error will return if cost is not reasonable

                        // add to DB
                        int max = 0;
                        String databaseURL2 = "jdbc:derby:inventory";
                        Connection con2 = DriverManager.getConnection(databaseURL2);

                        Statement st2 = con2.createStatement();

                        String sql2 = "select sid from clothes";
                        ResultSet rs2 = st2.executeQuery(sql2);

                        while (rs2.next() == true) // does something exist in the resultset
                        {
                            if (rs2.getInt(1) > max) {
                                max = rs2.getInt(1);
                            }

                        }

                        con2.close();
                        max++; // get maximum id and increment to get current id
                        DefaultTableModel model = (DefaultTableModel) tblClothes.getModel();

                        
                        LocalDate currentDate = LocalDate.now();
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                        String currentDateAsString = currentDate.format(formatter);
                        

                        
                        int rownum = tblClothes.getSelectedRow();

                        if (rownum == -1) {
                            JOptionPane.showMessageDialog(null, "Please select a row");
                        } else {
                            String databaseURL69 = "jdbc:derby:inventory";
                            Connection con69 = DriverManager.getConnection(databaseURL69);
                            Statement st69 = con69.createStatement();
                            int idupdate = Integer.parseInt(model.getValueAt(rownum, 0).toString());

                            String sql69 = "UPDATE clothes SET name = '" + txtName.getText() + "', email = '" + txtEmail.getText() + "', item = '" + cmbItem.getSelectedItem().toString() + "', size = '" + txtSize.getText().toUpperCase() + "', cost = " + Double.valueOf(txtCost.getText()) + ",status = '" + cmbStatus.getSelectedItem().toString() + "', commission = " + Double.valueOf(txtCommission.getText()) + ", notes = '" + tarNotes.getText() + "' WHERE sid = " + idupdate + "";
                            st69.executeUpdate(sql69);
                            con69.close();
                            String databaseURL = "jdbc:derby:inventory";
                            Connection con = DriverManager.getConnection(databaseURL);
                            Statement st = con.createStatement();
                            String sql = "select * from clothes";
                            ResultSet rs = st.executeQuery(sql);

                            model.setRowCount(0);// clearing the table
                            ArrayList<String> status = new ArrayList<String>();
                            while (rs.next() == true) // does something exist in the resultset
                            {
                                status.add(rs.getString(7));
                                model.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getDouble(6), rs.getString(7), rs.getDouble(8), rs.getString(9), rs.getString(10)}); // not 0 indexed find id, name, age and then email
                            }
                            int countAvailable = Collections.frequency(status, "AVAILABLE");
                            int countReserved = Collections.frequency(status, "RESERVED");
                            int countDamaged = Collections.frequency(status, "DAMAGED");
                            int countSold = Collections.frequency(status, "SOLD");
//                        tblClothes.getColumnModel().getColumn(10).setCellRenderer(new TableActionCellRender());/
                            con.close();

                            //clear it all
                            txtName.setText("");
                            txtEmail.setText("");
                            txtCost.setText("");
                            txtSize.setText("");
                            tarNotes.setText("");
                            txtCommission.setText("");
                            cmbItem.setSelectedIndex(0);
                            cmbStatus.setSelectedIndex(0);
                        }

                        
                        

                    } catch (Exception e) {

                        JOptionPane.showMessageDialog(null, "PLEASE FILL IN A VALID COST/COMMISSION. TRY REMOVING SYMBOLS.");
                    }
                }
            }
        } catch (Exception E){

        }
    }                                         

    private void tblClothesMouseClicked(java.awt.event.MouseEvent evt) {                                        
                
    }                                       

    private void txtSearchActionPerformed(java.awt.event.ActionEvent evt) {                                          
        // TODO add your handling code here:
    }                                         

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {                                          
        // TODO add your handling code here:
        try{
            DefaultTableModel model = (DefaultTableModel) tblClothes.getModel();
            String databaseURL = "jdbc:derby:inventory";
            Connection con = DriverManager.getConnection(databaseURL);
            Statement st = con.createStatement();

            //original QUERY

            con = DriverManager.getConnection(databaseURL);
            st = con.createStatement();
            String sql;
            try{
                int id = Integer.parseInt(txtSearch.getText());
                sql = "SELECT * FROM CLOTHES WHERE sid = "+id+"";
            } catch (Exception isnotinteger){
                String searchvalue = "%"+txtSearch.getText()+"%"; //regex is dubs
                sql = "SELECT * FROM CLOTHES WHERE NAME LIKE '"+searchvalue+"' OR EMAIL LIKE '"+searchvalue+"' OR NOTES LIKE '"+searchvalue+"' ORDER BY SID";
            }
            
            ResultSet rs = st.executeQuery(sql);
            model.setRowCount(0);// clearing the table

            while (rs.next() == true) // does something exist in the resultset
            {
                model.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getDouble(6), rs.getString(7), rs.getDouble(8), rs.getString(9), rs.getString(10)}); // not 0 indexed find id, name, age and then email
            }
            con.close();
            
        } catch (Exception E){

            
        }
        
        
        
    }                                         

    private void cmbItemActionPerformed(java.awt.event.ActionEvent evt) {                                        
        // TODO add your handling code here:
    }                                       

    private void btnResetActionPerformed(java.awt.event.ActionEvent evt) {                                         
        // TODO add your handling code here:
        try{
            DefaultTableModel model = (DefaultTableModel) tblClothes.getModel();
            String databaseURL = "jdbc:derby:inventory";
            Connection con = DriverManager.getConnection(databaseURL);
            Statement st = con.createStatement();
            String sql = "select * from clothes";
            ResultSet rs = st.executeQuery(sql);

            model.setRowCount(0);// clearing the table

            while (rs.next() == true) // does something exist in the resultset
            {
                model.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getDouble(6), rs.getString(7), rs.getDouble(8), rs.getString(9), rs.getString(10)}); // not 0 indexed find id, name, age and then email
            }
            con.close();

            //clear it all
            txtName.setText("");
            txtEmail.setText("");
            txtCost.setText("");
            txtSize.setText("");
            tarNotes.setText("");
            txtCommission.setText("");
            cmbItem.setSelectedIndex(0);
            cmbStatus.setSelectedIndex(0);
        } catch (Exception E){
            
        }
        
    }                                        

    private void txtCommissionActionPerformed(java.awt.event.ActionEvent evt) {                                              
        // TODO add your handling code here:
    }                                             

    private void cmbStatusActionPerformed(java.awt.event.ActionEvent evt) {                                          
        // TODO add your handling code here:
    }                                         
    
//    private void tableColumnSize(){
//        tblClothes.getColumn
//    }
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Home.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Home.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Home.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Home.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Home().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify                     
    private javax.swing.JButton btnClear;
    private javax.swing.JButton btnReset;
    private javax.swing.JButton btnSave;
    private javax.swing.JButton btnSearch;
    private javax.swing.JButton btnUpdate;
    private javax.swing.JComboBox<String> cmbItem;
    private javax.swing.JComboBox<String> cmbStatus;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextArea tarNotes;
    private javax.swing.JTable tblClothes;
    private javax.swing.JTextField txtCommission;
    private javax.swing.JTextField txtCost;
    private javax.swing.JTextField txtEmail;
    private javax.swing.JTextField txtName;
    private javax.swing.JTextField txtSearch;
    private javax.swing.JTextField txtSize;
    // End of variables declaration                   
}
