package smartmonkey.screen;

/**
 * + BUGS PARA RESOLVER COM LOG + FALHA CONEXÃO COM INTERNET/BD + QDO PASSOU +
 * LIMITES ESTIPULADOS + pegar data hora + conectar com BD
 */
// <editor-fold defaultstate="collapsed" desc="imports">
import java.awt.Color;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import javax.swing.JOptionPane;
import oshi.*;
import smartmonkey.ferramentas.ImageImporter;
import smartmonkey.bd.Maquina;
import smartmonkey.bd.MaquinaDAO;
import static javax.swing.JOptionPane.*;
import smartlog.FolderHandler;
import smartlog.LogFileHandler;
import smartlog.FileHandler;
import smartmonkey.bd.Leitura;
//</editor-fold>

/**
 *
 * @author tiago penha pedroso, 2019-06-08
 */
public class LocalMonitoring extends javax.swing.JFrame {

    private boolean enableDebugUpdateLabels = true;
    private int indexOfCurrentDisk;
    private int indexOfCurrentNetworkInterface;
    private int maxProcesses = 10;

    private List<Maquina> maquinas;
    private int indexOfSelectedMachine = -1;

    private Color buttonNormalColor = new Color(60, 63, 66);
    private Color buttonPressedColor = new Color(154, 47, 69); //255,0,51

    private Leitura leituraAtual = new Leitura();
    private double valorAtualPorcProcessador;
    private double valorAtualPorcRam;
    private double valorAtualPorcHd;
    private String valorAtualProcessos;
    private int valorAtualIdMaquina;
    private LogFileHandler log = new LogFileHandler(FolderHandler.USER_PATH + "/", "logsSmartMonkey", "errorLogs", "csv");
    private String logPath = FolderHandler.USER_PATH + "/" + this.log.getFolderName() + "/" + this.log.getFileName() + "." + this.log.getFileExtension();
    private boolean logEnable;
    private int tempoAtual;

    // <editor-fold defaultstate="collapsed" desc="constructor">
    public LocalMonitoring() {
        initComponents();
        moveJFrameToCenterOfScreen();

        showMessageDialog(
                null,
                "Starting a Database Connection :)\n\n"
                + "Close this pop-up and wait at most 1min...\n",
                "Database Connection",
                PLAIN_MESSAGE
        );

        createComboBoxMachines();

        log.addHeader(new String[]{"date", "idmachine", "cpu", "ram", "disk", "processes (PID, %CPU, %RAM)"});

        initStaticTexts();
        updateLabels();
        updateButtonGravarDados();

        mudarCorPainelAlerta();
    }
    //</editor-fold>

    private void updateIndexOfSelectedMachine() {
        //Existe itens na lista ComboBox?
        if (comboBoxHostId.getItemCount() > 0) {
            indexOfSelectedMachine = comboBoxHostId.getSelectedIndex();
            indexOfSelectedMachine = indexOfSelectedMachine < 0 ? 0 : indexOfSelectedMachine;
            if (maquinas != null) {
				valorAtualIdMaquina = maquinas.get(indexOfSelectedMachine).getIdMaquina();
			}
        }
    }

    private void createComboBoxMachines() {
        indexOfSelectedMachine = comboBoxHostId.getSelectedIndex();
        indexOfSelectedMachine = indexOfSelectedMachine < 0 ? 0 : indexOfSelectedMachine;

        maquinas = MaquinaDAO.getAll();

        if (maquinas != null) {
            comboBoxHostId.removeAllItems();

            for (int count = 0; count < maquinas.size(); count++) {
                comboBoxHostId.addItem("Machine [" + maquinas.get(count).getIdMaquina() + "]: " + maquinas.get(count).getModelo());
            }
			
            comboBoxHostId.setSelectedIndex(indexOfSelectedMachine);
        }
    }

    // <editor-fold defaultstate="collapsed" desc="initStaticTexts()">   
    private void initStaticTexts() {
        //Logs
        labelLogUrl.setText(logPath);

        //cpu
        labelCpuFabricante.setText(SimpleSystemInfo.getCpuManufacturer());
        labelCpuModelo.setText(SimpleSystemInfo.getCpuModel());
        labelCpuCoresQtd.setText(String.valueOf(SimpleSystemInfo.getCpuNumberOfLogicalCores()));

        //ram
        labelRamFabricante.setText(SimpleSystemInfo.getMemoryManufacturer());

        //network
        comboBoxNetworkInterfacesName.removeAllItems();
        String[] networkInterfacesName = SimpleSystemInfo.getNetworkInterfaceNames();
        for (int count = 0; count < networkInterfacesName.length; count++) {
            comboBoxNetworkInterfacesName.addItem("Interface [" + count + "]: " + networkInterfacesName[count]);
        }
        indexOfCurrentNetworkInterface = 0;
        labelNetworkFabricante.setText(SimpleSystemInfo.getNetworkInterfaceManufacturer(indexOfCurrentNetworkInterface));

        //disk
        comboBoxSelectDisk.removeAllItems();
        String[] disksName = SimpleSystemInfo.getDisksMountName();
        String[] disksFileSystem = SimpleSystemInfo.getDisksFormatFileSystem();
        String[] disksType = SimpleSystemInfo.getDisksType();
        for (int count = 0; count < disksName.length; count++) {
            if (disksFileSystem[count].equalsIgnoreCase("")) {
                comboBoxSelectDisk.addItem("Disk [" + count + "]: " + disksName[count] + " - " + disksType[count]);
            } else {
                comboBoxSelectDisk.addItem("Disk [" + count + "]: " + disksName[count] + " - " + disksFileSystem[count]);
            }
        }
        indexOfCurrentDisk = 0;

        temp = "";
        for (int count = 0; count < SimpleSystemInfo.getDisksNumber(); count++) {
            if (!SimpleSystemInfo.getDisksManufacturer()[count].equalsIgnoreCase("")) {
                temp += SimpleSystemInfo.getDisksManufacturer()[count];
            }
        }
        labelDiskFabricante.setText(temp);

        //os
        imageOS.setIcon(ImageImporter.iconOs);
        labelOSFabricante.setText(SimpleSystemInfo.getOsManufacturer());
        labelOSVersao.setText(
                SimpleSystemInfo.getOsName() + ", "
                + SimpleSystemInfo.getOsVersion() + " ("
                + SimpleSystemInfo.getOsBuildVersion() + ")"
        );
        labelOSTipoSistema.setText(SimpleSystemInfo.getOsSystemTypeArchitecture());
    }
    //</editor-fold>

    // <editor-fold defaultstate="collapsed" desc="updateLabels()">
    private void updateLabels() {
        // <editor-fold defaultstate="collapsed" desc="init Thread Looping">   
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    //</editor-fold>

                    //cpu                    
                    valorAtualPorcProcessador = SimpleSystemInfo.getCpuUsedPercentageAsDouble();
                    labelCpuUso.setText(String.valueOf(valorAtualPorcProcessador));

                    labelCpuFrequencia.setText(String.valueOf(SimpleSystemInfo.getCpuManufacturerFrequencyAsDouble()));

                    //ram
                    valorAtualPorcRam = SimpleSystemInfo.getMemoryUsedPercentageAsDouble();
                    labelRamUso.setText(String.valueOf(valorAtualPorcRam));

                    labelRamUsoGb.setText(String.valueOf(SimpleSystemInfo.getMemoryUsedAsDouble()));
                    labelRamTotalGb.setText(String.valueOf(SimpleSystemInfo.getMemoryCapacityAsDouble()));
                    labelRamVirtualUsoGb.setText(String.valueOf(SimpleSystemInfo.getVirtualMemoryUsedPercentageAsDouble()));
                    labelRamVirtualTotalGb.setText(String.valueOf(SimpleSystemInfo.getVirtualMemoryCapacityAsString()));

                    //disk                    
                    valorAtualPorcHd = SimpleSystemInfo.getDisksUsedPercentageAsDouble()[indexOfCurrentDisk];
                    labelDiskUso.setText(String.valueOf(valorAtualPorcHd));

                    labelDiskUsoGb.setText(String.valueOf(SimpleSystemInfo.getDisksUsedAsDouble()[indexOfCurrentDisk]));
                    labelDiskTotalGb.setText(String.valueOf(SimpleSystemInfo.getDisksCapacityAsDouble()[indexOfCurrentDisk]));

                    //network
                    labelNetworkFabricante.setText(SimpleSystemInfo.getNetworkInterfaceManufacturer(indexOfCurrentNetworkInterface));
                    labelNetworkNome.setText(SimpleSystemInfo.getNetworkHostName());
                    labelNetworkIp.setText(SimpleSystemInfo.getNetworkInterfaceIp4(indexOfCurrentNetworkInterface));
                    labelNetworkDns.setText(SimpleSystemInfo.getNetworkDnsServers());
                    labelNetworkGateway.setText(SimpleSystemInfo.getNetworkGatewayIp4());
                    labelNetworkMac.setText(SimpleSystemInfo.getNetworkInterfaceMacAdress(indexOfCurrentNetworkInterface));

                    //os                    
                    labelOSTotalProcessos.setText(String.valueOf(SimpleSystemInfo.getProcessesNumber()));
                    labelOSTotalThreads.setText(String.valueOf(SimpleSystemInfo.getProcessesThreadNumber()));

                    //process
                    tempName = tempPid = tempCpuPercent = tempRam = tempRamPercent = valorAtualProcessos = "";
                    for (String[] process : SimpleSystemInfo.getProcessesAsStringTable(maxProcesses)) {
                        tempName += (process[0].length() > 13 ? process[0].substring(0, 13) : process[0]) + "\n";
                        tempPid += process[1] + "\n";
                        tempCpuPercent += process[2] + "\n";
                        tempRam += process[3] + "\n";
                        tempRamPercent += process[4] + "\n";
                        valorAtualProcessos += ((process[0].length() > 13 ? process[0].substring(0, 13) : process[0])
                                + "([" + process[1] + "],[" + process[2] + "],[" + process[4] + "]) | ");
                    }
                    labelProcessName.setText(tempName.substring(0, tempName.length() - 1));
                    labelProcessPid.setText(tempPid.substring(0, tempPid.length() - 1));
                    labelProcessCpuPercent.setText(tempCpuPercent.substring(0, tempCpuPercent.length() - 1));
                    labelProcessRam.setText(tempRam.substring(0, tempRam.length() - 1));
                    labelProcessRamPercent.setText(tempRamPercent.substring(0, tempRamPercent.length() - 1));

                    //Update
                    //Gravando dados no Banco
                    if (buttonGravarDados.isSelected()) {
                        leituraAtual.setPorcProcessador(valorAtualPorcProcessador);
                        leituraAtual.setPorcRam(valorAtualPorcRam);
                        leituraAtual.setPorcHd(valorAtualPorcHd);
                        leituraAtual.setIdMaquina(valorAtualIdMaquina);

                        leituraAtual.setDataLeitura(
                                Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
                                (Calendar.getInstance().get(Calendar.MONTH) + 1),
                                //                                5,
                                Calendar.getInstance().get(Calendar.YEAR),
                                Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                                Calendar.getInstance().get(Calendar.MINUTE),
                                Calendar.getInstance().get(Calendar.SECOND),
                                Calendar.getInstance().get(Calendar.MILLISECOND)
                        );

                        leituraAtual.salvarDados();

                        //preparando para próxima iteração;
                        leituraAtual.setIdLeitura(-1);
                    }

                    verifyLimitsAndSaveLog();

                    // <editor-fold defaultstate="collapsed" desc="end Thread Looping">   
                    try {
                        countDebugUpdate++;

                        if (enableDebugUpdateLabels) {
                            labelLogUrl.setVisible(true);
                            labelCurrentUpdate.setVisible(true);
                            labelCurrentUpdate.setText(String.valueOf(countDebugUpdate));
                        } else {
                            labelLogUrl.setVisible(false);
                            labelCurrentUpdate.setVisible(false);
                        }

                        Thread.sleep(5);
                    } catch (Exception e) {
                    }
                }
            }
        }.start();
        //</editor-fold>
    }
    //</editor-fold>

    // <editor-fold defaultstate="collapsed" desc="updateButtonGravarDados()">
    private void updateButtonGravarDados() {
        if (maquinas != null) {
            if (buttonGravarDados.isSelected()) {
                buttonGravarDados.setBackground(buttonPressedColor);
                buttonGravarDados.setText("Sending data...");
                comboBoxHostId.setEnabled(false);
            } else {
                buttonGravarDados.setBackground(buttonNormalColor);
                buttonGravarDados.setText("Write Data to the Database");
                comboBoxHostId.setEnabled(true);
            }
        } else {
            buttonGravarDados.setEnabled(false);
            comboBoxHostId.setEnabled(false);

            showMessageDialog(
                    null,
                    "Unfortunately it was not possible to connect to the database :(\n\n"
                    + "If it is necessary to save the data, follow these steps:\n"
                    + "  1) Close this app\n"
                    + "  2) Solve your connection problem\n"
                    + "  3) Start this app again",
                    "Connection Error",
                    PLAIN_MESSAGE
            );
        }
    }
    //</editor-fold>

    private void verifyLimitsAndSaveLog() {
        updateIndexOfSelectedMachine();

        double delimitCpu = 200;
        double delimitRam = 200;
        double delimitHd = 200;

        if (maquinas != null) {
            delimitCpu = maquinas.get(indexOfSelectedMachine).getDelimitCpu();
            delimitRam = maquinas.get(indexOfSelectedMachine).getDelimitRam();
            delimitHd = maquinas.get(indexOfSelectedMachine).getDelimitHd();
            labelDelimits.setText("\"" + maquinas.get(indexOfSelectedMachine).getModelo() + "\": CPU " + delimitCpu + "%, RAM " + delimitRam + "%, HD " + delimitHd + "%");
        } else {
            labelDelimits.setText("\"MODEL\": CPU _%, RAM _%, HD _%");
        }

        if (this.valorAtualPorcProcessador > delimitCpu
                || this.valorAtualPorcRam > delimitRam
                || this.valorAtualPorcHd > delimitHd) {

            this.log.addLineWithDateTime(
                    String.valueOf(this.valorAtualIdMaquina),
                    String.valueOf(this.valorAtualPorcProcessador),
                    String.valueOf(this.valorAtualPorcRam),
                    String.valueOf(this.valorAtualPorcHd),
                    this.valorAtualProcessos
            );
            
            logEnable = true;
        } else {
            logEnable = false;
        }
    }

    public void mudarCorPainelAlerta() {
        // <editor-fold defaultstate="collapsed" desc="init Thread Looping">   
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    //</editor-fold>

                    tempoAtual = (int) (System.currentTimeMillis() / 1000.0);

                    if (logEnable && tempoAtual % 2 == 0) {
                        painelAlerta.setBackground(buttonPressedColor);
                        jLabel2.setVisible(true);
                    } else {
                        painelAlerta.setBackground(Color.BLACK);
                        jLabel2.setVisible(false);
                    }

                    // <editor-fold defaultstate="collapsed" desc="end Thread Looping">   
                    try {
                        Thread.sleep(80);
                    } catch (Exception e) {
                    }
                }
            }
        }.start();
        //</editor-fold>
    }

    //<editor-fold defaultstate="collapsed" desc="method: initComponents()">
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        panelMonitoramento = new javax.swing.JPanel();
        painelAlerta = new javax.swing.JPanel();
        labelHostId1 = new javax.swing.JLabel();
        comboBoxHostId = new javax.swing.JComboBox<>();
        jLabel2 = new javax.swing.JLabel();
        painelSuperior = new javax.swing.JPanel();
        labelMonitoramentoPC = new javax.swing.JLabel();
        labelMonitoramentoPC1 = new javax.swing.JLabel();
        imageLogoMonitoramento = new javax.swing.JLabel();
        panelCpu = new javax.swing.JPanel();
        imageCpu = new javax.swing.JLabel();
        labelCpuFabricante = new javax.swing.JLabel();
        labelCpuUso = new javax.swing.JLabel();
        labelCpuSimboloPorcentagem = new javax.swing.JLabel();
        labelCpuModelo = new javax.swing.JLabel();
        labelCpuFrequencia = new javax.swing.JLabel();
        labelCpuCoresQtd = new javax.swing.JLabel();
        labelCpuCoresText = new javax.swing.JLabel();
        labelCpuFrequenciaGhz = new javax.swing.JLabel();
        panelRam = new javax.swing.JPanel();
        imageRam = new javax.swing.JLabel();
        labelRamFabricante = new javax.swing.JLabel();
        labelRamUso = new javax.swing.JLabel();
        labelRamSimboloPorcentagem = new javax.swing.JLabel();
        labelRamUsoGb = new javax.swing.JLabel();
        labelRamTotalGb = new javax.swing.JLabel();
        labelRamCoresText = new javax.swing.JLabel();
        labelRamFrequenciaGhz = new javax.swing.JLabel();
        labelRamSimboloBarra = new javax.swing.JLabel();
        labelRamFrequenciaGhz1 = new javax.swing.JLabel();
        labelRamCoresText1 = new javax.swing.JLabel();
        labelRamCoresText2 = new javax.swing.JLabel();
        labelRamVirtualUsoGb = new javax.swing.JLabel();
        labelRamVirtualTotalGb = new javax.swing.JLabel();
        panelDisk = new javax.swing.JPanel();
        imageDisk = new javax.swing.JLabel();
        labelDiskFabricante = new javax.swing.JLabel();
        labelDiskUso = new javax.swing.JLabel();
        labelDiskSimboloPorcentagem = new javax.swing.JLabel();
        labelDiskUsoGb = new javax.swing.JLabel();
        labelDiskTotalGb = new javax.swing.JLabel();
        labelDiskCoresText = new javax.swing.JLabel();
        labelDiskFrequenciaGhz = new javax.swing.JLabel();
        labelDiskSimboloBarra = new javax.swing.JLabel();
        comboBoxSelectDisk = new javax.swing.JComboBox<>();
        panelNetwork = new javax.swing.JPanel();
        imageNetwork = new javax.swing.JLabel();
        labelNetworkFabricante = new javax.swing.JLabel();
        labelNetworkTextNome = new javax.swing.JLabel();
        labelNetworkNome = new javax.swing.JLabel();
        labelNetworkTextIp = new javax.swing.JLabel();
        labelNetworkIp = new javax.swing.JLabel();
        labelNetworkDns = new javax.swing.JLabel();
        labelNetworkSimboloPorcentagem4 = new javax.swing.JLabel();
        labelNetworkSimboloPorcentagem5 = new javax.swing.JLabel();
        labelNetworkGateway = new javax.swing.JLabel();
        comboBoxNetworkInterfacesName = new javax.swing.JComboBox<>();
        labelNetworkMac = new javax.swing.JLabel();
        labelNetworkTextIp1 = new javax.swing.JLabel();
        panelOS = new javax.swing.JPanel();
        imageOS = new javax.swing.JLabel();
        labelOSFabricante = new javax.swing.JLabel();
        labelOSVersao = new javax.swing.JLabel();
        labelOSSimboloPorcentagem = new javax.swing.JLabel();
        labelOSTipoSistema = new javax.swing.JLabel();
        labelOSTotalProcessos = new javax.swing.JLabel();
        labelOSFrequenciaGhz = new javax.swing.JLabel();
        labelOSSimboloPorcentagem1 = new javax.swing.JLabel();
        labelOSSimboloPorcentagem2 = new javax.swing.JLabel();
        labelOSTotalThreads = new javax.swing.JLabel();
        panelProcess = new javax.swing.JPanel();
        imageProcess = new javax.swing.JLabel();
        labelProcessSimboloPorcentagem3 = new javax.swing.JLabel();
        labelProcessSimboloPorcentagem5 = new javax.swing.JLabel();
        labelProcessSimboloPorcentagem6 = new javax.swing.JLabel();
        labelProcessSimboloPorcentagem7 = new javax.swing.JLabel();
        labelProcessSimboloPorcentagem8 = new javax.swing.JLabel();
        labelProcessSimboloPorcentagem9 = new javax.swing.JLabel();
        labelProcessName = new javax.swing.JTextArea();
        labelProcessPid = new javax.swing.JTextArea();
        labelProcessCpuPercent = new javax.swing.JTextArea();
        labelProcessRam = new javax.swing.JTextArea();
        labelProcessRamPercent = new javax.swing.JTextArea();
        labelLogUrl = new javax.swing.JLabel();
        labelLogUrlTitle = new javax.swing.JLabel();
        labelCurrentUpdate = new javax.swing.JLabel();
        buttonGravarDados = new javax.swing.JToggleButton();
        labelTextCurrenteUpdate1 = new javax.swing.JLabel();
        labelDelimitsTitle = new javax.swing.JLabel();
        labelDelimits = new javax.swing.JLabel();
        panelSobreNos = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(956, 670));
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                OnWindowCloese(evt);
            }
        });

        jTabbedPane1.setBackground(new java.awt.Color(0, 0, 1));
        jTabbedPane1.setForeground(new java.awt.Color(187, 187, 186));

        panelMonitoramento.setBackground(new java.awt.Color(51, 51, 51));
        panelMonitoramento.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        painelAlerta.setBackground(new java.awt.Color(0, 0, 0));
        painelAlerta.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        labelHostId1.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        labelHostId1.setForeground(new java.awt.Color(153, 153, 153));
        labelHostId1.setText("Host ID");
        painelAlerta.add(labelHostId1, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 20, -1, 20));

        comboBoxHostId.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        comboBoxHostId.setForeground(new java.awt.Color(187, 187, 186));
        comboBoxHostId.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Database Connection Error  :(" }));
        comboBoxHostId.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                comboBoxHostIdOnComboBoxChangeState_HostId(evt);
            }
        });
        painelAlerta.add(comboBoxHostId, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 50, 316, -1));

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/smartmonkeymonitoramento/images/alert-icon.png"))); // NOI18N
        painelAlerta.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 0, 90, 90));

        panelMonitoramento.add(painelAlerta, new org.netbeans.lib.awtextra.AbsoluteConstraints(530, 0, 480, 90));

        painelSuperior.setBackground(new java.awt.Color(0, 0, 0));
        painelSuperior.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        labelMonitoramentoPC.setFont(new java.awt.Font("Arial Black", 0, 30)); // NOI18N
        labelMonitoramentoPC.setForeground(new java.awt.Color(102, 102, 102));
        labelMonitoramentoPC.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelMonitoramentoPC.setText("Panel");
        painelSuperior.add(labelMonitoramentoPC, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 40, 320, 40));

        labelMonitoramentoPC1.setFont(new java.awt.Font("Arial Black", 0, 30)); // NOI18N
        labelMonitoramentoPC1.setForeground(new java.awt.Color(102, 102, 102));
        labelMonitoramentoPC1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelMonitoramentoPC1.setText("Monitoring");
        painelSuperior.add(labelMonitoramentoPC1, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 10, 280, 40));

        imageLogoMonitoramento.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        imageLogoMonitoramento.setIcon(new javax.swing.ImageIcon(getClass().getResource("/smartmonkeymonitoramento/images/logo-smartmonkey-white-landscape_small.png"))); // NOI18N
        painelSuperior.add(imageLogoMonitoramento, new org.netbeans.lib.awtextra.AbsoluteConstraints(6, 6, 190, 70));

        panelMonitoramento.add(painelSuperior, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 530, 90));

        panelCpu.setBackground(new java.awt.Color(60, 63, 66));
        panelCpu.setPreferredSize(new java.awt.Dimension(356, 130));

        imageCpu.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        imageCpu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/smartmonkeymonitoramento/images/icon_cpu.png"))); // NOI18N

        labelCpuFabricante.setFont(new java.awt.Font("Dialog", 1, 13)); // NOI18N
        labelCpuFabricante.setForeground(new java.awt.Color(107, 107, 107));
        labelCpuFabricante.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelCpuFabricante.setText("<manufacturer>");

        labelCpuUso.setFont(new java.awt.Font("Dialog", 1, 48)); // NOI18N
        labelCpuUso.setForeground(new java.awt.Color(255, 255, 255));
        labelCpuUso.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelCpuUso.setText("0");

        labelCpuSimboloPorcentagem.setBackground(new java.awt.Color(102, 102, 102));
        labelCpuSimboloPorcentagem.setFont(new java.awt.Font("Dialog", 1, 24)); // NOI18N
        labelCpuSimboloPorcentagem.setForeground(new java.awt.Color(107, 107, 107));
        labelCpuSimboloPorcentagem.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelCpuSimboloPorcentagem.setText("%");

        labelCpuModelo.setFont(new java.awt.Font("Dialog", 1, 13)); // NOI18N
        labelCpuModelo.setForeground(new java.awt.Color(255, 255, 255));
        labelCpuModelo.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelCpuModelo.setText("<model>");

        labelCpuFrequencia.setFont(new java.awt.Font("Dialog", 1, 28)); // NOI18N
        labelCpuFrequencia.setForeground(new java.awt.Color(255, 255, 255));
        labelCpuFrequencia.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelCpuFrequencia.setText("0");

        labelCpuCoresQtd.setFont(new java.awt.Font("Dialog", 1, 24)); // NOI18N
        labelCpuCoresQtd.setForeground(new java.awt.Color(255, 255, 255));
        labelCpuCoresQtd.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelCpuCoresQtd.setText("0");

        labelCpuCoresText.setFont(new java.awt.Font("Dialog", 1, 13)); // NOI18N
        labelCpuCoresText.setForeground(new java.awt.Color(107, 107, 107));
        labelCpuCoresText.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelCpuCoresText.setText("cores");

        labelCpuFrequenciaGhz.setFont(new java.awt.Font("Dialog", 1, 13)); // NOI18N
        labelCpuFrequenciaGhz.setForeground(new java.awt.Color(107, 107, 107));
        labelCpuFrequenciaGhz.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelCpuFrequenciaGhz.setText("Ghz");

        javax.swing.GroupLayout panelCpuLayout = new javax.swing.GroupLayout(panelCpu);
        panelCpu.setLayout(panelCpuLayout);
        panelCpuLayout.setHorizontalGroup(
            panelCpuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCpuLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addComponent(imageCpu, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addGroup(panelCpuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelCpuLayout.createSequentialGroup()
                        .addGap(4, 4, 4)
                        .addComponent(labelCpuUso, javax.swing.GroupLayout.PREFERRED_SIZE, 197, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(labelCpuSimboloPorcentagem, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelCpuLayout.createSequentialGroup()
                        .addComponent(labelCpuFabricante, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(labelCpuModelo, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelCpuLayout.createSequentialGroup()
                        .addComponent(labelCpuFrequencia, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(labelCpuFrequenciaGhz, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(labelCpuCoresText, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(labelCpuCoresQtd, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelCpuLayout.setVerticalGroup(
            panelCpuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCpuLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(imageCpu, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(panelCpuLayout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(panelCpuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelCpuUso, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelCpuSimboloPorcentagem))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelCpuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelCpuCoresQtd, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelCpuCoresText)
                    .addComponent(labelCpuFrequenciaGhz, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelCpuFrequencia, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(9, 9, 9)
                .addGroup(panelCpuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labelCpuFabricante)
                    .addComponent(labelCpuModelo)))
        );

        panelMonitoramento.add(panelCpu, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 110, 357, -1));

        panelRam.setBackground(new java.awt.Color(60, 63, 66));
        panelRam.setPreferredSize(new java.awt.Dimension(356, 130));

        imageRam.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        imageRam.setIcon(new javax.swing.ImageIcon(getClass().getResource("/smartmonkeymonitoramento/images/icon_ram.png"))); // NOI18N

        labelRamFabricante.setFont(new java.awt.Font("Dialog", 1, 13)); // NOI18N
        labelRamFabricante.setForeground(new java.awt.Color(107, 107, 107));
        labelRamFabricante.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelRamFabricante.setText("<manufacturer>");

        labelRamUso.setFont(new java.awt.Font("Dialog", 1, 48)); // NOI18N
        labelRamUso.setForeground(new java.awt.Color(255, 255, 255));
        labelRamUso.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelRamUso.setText("0");

        labelRamSimboloPorcentagem.setBackground(new java.awt.Color(102, 102, 102));
        labelRamSimboloPorcentagem.setFont(new java.awt.Font("Dialog", 1, 24)); // NOI18N
        labelRamSimboloPorcentagem.setForeground(new java.awt.Color(107, 107, 107));
        labelRamSimboloPorcentagem.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelRamSimboloPorcentagem.setText("%");

        labelRamUsoGb.setFont(new java.awt.Font("Dialog", 1, 32)); // NOI18N
        labelRamUsoGb.setForeground(new java.awt.Color(255, 255, 255));
        labelRamUsoGb.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelRamUsoGb.setText("0");

        labelRamTotalGb.setFont(new java.awt.Font("Dialog", 1, 20)); // NOI18N
        labelRamTotalGb.setForeground(new java.awt.Color(255, 255, 255));
        labelRamTotalGb.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelRamTotalGb.setText("0");

        labelRamCoresText.setFont(new java.awt.Font("Dialog", 1, 13)); // NOI18N
        labelRamCoresText.setForeground(new java.awt.Color(107, 107, 107));
        labelRamCoresText.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelRamCoresText.setText("GiB");

        labelRamFrequenciaGhz.setFont(new java.awt.Font("Dialog", 1, 13)); // NOI18N
        labelRamFrequenciaGhz.setForeground(new java.awt.Color(107, 107, 107));
        labelRamFrequenciaGhz.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelRamFrequenciaGhz.setText("GiB");

        labelRamSimboloBarra.setBackground(new java.awt.Color(102, 102, 102));
        labelRamSimboloBarra.setFont(new java.awt.Font("Dialog", 1, 24)); // NOI18N
        labelRamSimboloBarra.setForeground(new java.awt.Color(107, 107, 107));
        labelRamSimboloBarra.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelRamSimboloBarra.setText("/");

        labelRamFrequenciaGhz1.setFont(new java.awt.Font("Dialog", 1, 13)); // NOI18N
        labelRamFrequenciaGhz1.setForeground(new java.awt.Color(107, 107, 107));
        labelRamFrequenciaGhz1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelRamFrequenciaGhz1.setText("swap");

        labelRamCoresText1.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        labelRamCoresText1.setForeground(new java.awt.Color(107, 107, 107));
        labelRamCoresText1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelRamCoresText1.setText("%");

        labelRamCoresText2.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        labelRamCoresText2.setForeground(new java.awt.Color(107, 107, 107));
        labelRamCoresText2.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelRamCoresText2.setText("/");

        labelRamVirtualUsoGb.setFont(new java.awt.Font("Dialog", 1, 18)); // NOI18N
        labelRamVirtualUsoGb.setForeground(new java.awt.Color(255, 255, 255));
        labelRamVirtualUsoGb.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelRamVirtualUsoGb.setText("0");

        labelRamVirtualTotalGb.setFont(new java.awt.Font("Dialog", 1, 18)); // NOI18N
        labelRamVirtualTotalGb.setForeground(new java.awt.Color(255, 255, 255));
        labelRamVirtualTotalGb.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelRamVirtualTotalGb.setText("0");

        javax.swing.GroupLayout panelRamLayout = new javax.swing.GroupLayout(panelRam);
        panelRam.setLayout(panelRamLayout);
        panelRamLayout.setHorizontalGroup(
            panelRamLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(panelRamLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelRamLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelRamLayout.createSequentialGroup()
                        .addComponent(imageRam, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panelRamLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panelRamLayout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(labelRamUso, javax.swing.GroupLayout.PREFERRED_SIZE, 207, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(labelRamSimboloPorcentagem, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(panelRamLayout.createSequentialGroup()
                                .addComponent(labelRamUsoGb, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(6, 6, 6)
                                .addComponent(labelRamFrequenciaGhz, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(labelRamSimboloBarra, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(labelRamTotalGb, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(labelRamCoresText)
                                .addGap(0, 0, Short.MAX_VALUE))))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelRamLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(labelRamFabricante, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addComponent(labelRamFrequenciaGhz1, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addComponent(labelRamVirtualUsoGb, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addComponent(labelRamCoresText1)
                        .addGap(6, 6, 6)
                        .addComponent(labelRamCoresText2, javax.swing.GroupLayout.PREFERRED_SIZE, 5, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(labelRamVirtualTotalGb, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        panelRamLayout.setVerticalGroup(
            panelRamLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelRamLayout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(panelRamLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelRamLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(imageRam, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelRamLayout.createSequentialGroup()
                        .addGroup(panelRamLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(labelRamUso, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(labelRamSimboloPorcentagem))
                        .addGap(3, 3, 3)
                        .addGroup(panelRamLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(labelRamSimboloBarra)
                            .addComponent(labelRamFrequenciaGhz, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(labelRamUsoGb, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(labelRamTotalGb, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(labelRamCoresText, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelRamLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labelRamFabricante, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(panelRamLayout.createSequentialGroup()
                        .addGap(4, 4, 4)
                        .addComponent(labelRamFrequenciaGhz1, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelRamLayout.createSequentialGroup()
                        .addGap(2, 2, 2)
                        .addComponent(labelRamVirtualUsoGb, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelRamLayout.createSequentialGroup()
                        .addGap(8, 8, 8)
                        .addComponent(labelRamCoresText1))
                    .addGroup(panelRamLayout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addComponent(labelRamCoresText2, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelRamLayout.createSequentialGroup()
                        .addGap(2, 2, 2)
                        .addComponent(labelRamVirtualTotalGb, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        panelMonitoramento.add(panelRam, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 250, 357, 131));

        panelDisk.setBackground(new java.awt.Color(60, 63, 66));
        panelDisk.setPreferredSize(new java.awt.Dimension(356, 130));

        imageDisk.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        imageDisk.setIcon(new javax.swing.ImageIcon(getClass().getResource("/smartmonkeymonitoramento/images/icon_disk.png"))); // NOI18N

        labelDiskFabricante.setFont(new java.awt.Font("Dialog", 1, 13)); // NOI18N
        labelDiskFabricante.setForeground(new java.awt.Color(107, 107, 107));
        labelDiskFabricante.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelDiskFabricante.setText("<manufacturer>");

        labelDiskUso.setFont(new java.awt.Font("Dialog", 1, 48)); // NOI18N
        labelDiskUso.setForeground(new java.awt.Color(255, 255, 255));
        labelDiskUso.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelDiskUso.setText("0");

        labelDiskSimboloPorcentagem.setBackground(new java.awt.Color(102, 102, 102));
        labelDiskSimboloPorcentagem.setFont(new java.awt.Font("Dialog", 1, 24)); // NOI18N
        labelDiskSimboloPorcentagem.setForeground(new java.awt.Color(107, 107, 107));
        labelDiskSimboloPorcentagem.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelDiskSimboloPorcentagem.setText("%");

        labelDiskUsoGb.setFont(new java.awt.Font("Dialog", 1, 32)); // NOI18N
        labelDiskUsoGb.setForeground(new java.awt.Color(255, 255, 255));
        labelDiskUsoGb.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelDiskUsoGb.setText("0");

        labelDiskTotalGb.setFont(new java.awt.Font("Dialog", 1, 20)); // NOI18N
        labelDiskTotalGb.setForeground(new java.awt.Color(255, 255, 255));
        labelDiskTotalGb.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelDiskTotalGb.setText("0");

        labelDiskCoresText.setFont(new java.awt.Font("Dialog", 1, 13)); // NOI18N
        labelDiskCoresText.setForeground(new java.awt.Color(107, 107, 107));
        labelDiskCoresText.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelDiskCoresText.setText("GiB");

        labelDiskFrequenciaGhz.setFont(new java.awt.Font("Dialog", 1, 13)); // NOI18N
        labelDiskFrequenciaGhz.setForeground(new java.awt.Color(107, 107, 107));
        labelDiskFrequenciaGhz.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelDiskFrequenciaGhz.setText("GiB");

        labelDiskSimboloBarra.setBackground(new java.awt.Color(102, 102, 102));
        labelDiskSimboloBarra.setFont(new java.awt.Font("Dialog", 1, 24)); // NOI18N
        labelDiskSimboloBarra.setForeground(new java.awt.Color(107, 107, 107));
        labelDiskSimboloBarra.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelDiskSimboloBarra.setText("/");

        comboBoxSelectDisk.setBackground(new java.awt.Color(60, 63, 66));
        comboBoxSelectDisk.setFont(new java.awt.Font("Dialog", 1, 11)); // NOI18N
        comboBoxSelectDisk.setForeground(new java.awt.Color(255, 255, 255));
        comboBoxSelectDisk.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "HD 01 - C: - NTFS" }));
        comboBoxSelectDisk.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                OnItemStateChanged_ComboBoxSelectDisk(evt);
            }
        });

        javax.swing.GroupLayout panelDiskLayout = new javax.swing.GroupLayout(panelDisk);
        panelDisk.setLayout(panelDiskLayout);
        panelDiskLayout.setHorizontalGroup(
            panelDiskLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelDiskLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelDiskLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelDiskLayout.createSequentialGroup()
                        .addComponent(imageDisk)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panelDiskLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panelDiskLayout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(labelDiskUso, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(labelDiskSimboloPorcentagem))
                            .addGroup(panelDiskLayout.createSequentialGroup()
                                .addComponent(labelDiskUsoGb, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(labelDiskCoresText)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(labelDiskSimboloBarra)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(labelDiskTotalGb, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(labelDiskFrequenciaGhz))))
                    .addGroup(panelDiskLayout.createSequentialGroup()
                        .addComponent(labelDiskFabricante, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(comboBoxSelectDisk, javax.swing.GroupLayout.PREFERRED_SIZE, 187, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        panelDiskLayout.setVerticalGroup(
            panelDiskLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelDiskLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelDiskLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelDiskLayout.createSequentialGroup()
                        .addGroup(panelDiskLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(labelDiskUso, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(labelDiskSimboloPorcentagem))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(panelDiskLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(labelDiskTotalGb)
                            .addComponent(labelDiskUsoGb, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(labelDiskCoresText)
                            .addComponent(labelDiskSimboloBarra)
                            .addComponent(labelDiskFrequenciaGhz)))
                    .addComponent(imageDisk))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelDiskLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(comboBoxSelectDisk, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelDiskFabricante))
                .addContainerGap(11, Short.MAX_VALUE))
        );

        panelMonitoramento.add(panelDisk, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 390, -1, 138));

        panelNetwork.setBackground(new java.awt.Color(60, 63, 66));
        panelNetwork.setPreferredSize(new java.awt.Dimension(356, 130));

        imageNetwork.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        imageNetwork.setIcon(new javax.swing.ImageIcon(getClass().getResource("/smartmonkeymonitoramento/images/icon-network.png"))); // NOI18N

        labelNetworkFabricante.setFont(new java.awt.Font("Dialog", 1, 13)); // NOI18N
        labelNetworkFabricante.setForeground(new java.awt.Color(107, 107, 107));
        labelNetworkFabricante.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelNetworkFabricante.setText("<fabricante>");

        labelNetworkTextNome.setBackground(new java.awt.Color(102, 102, 102));
        labelNetworkTextNome.setFont(new java.awt.Font("Dialog", 1, 16)); // NOI18N
        labelNetworkTextNome.setForeground(new java.awt.Color(107, 107, 107));
        labelNetworkTextNome.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelNetworkTextNome.setText("Name");

        labelNetworkNome.setFont(new java.awt.Font("Dialog", 1, 24)); // NOI18N
        labelNetworkNome.setForeground(new java.awt.Color(255, 255, 255));
        labelNetworkNome.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelNetworkNome.setText("?");

        labelNetworkTextIp.setFont(new java.awt.Font("Dialog", 1, 13)); // NOI18N
        labelNetworkTextIp.setForeground(new java.awt.Color(107, 107, 107));
        labelNetworkTextIp.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelNetworkTextIp.setText("IP");

        labelNetworkIp.setFont(new java.awt.Font("Dialog", 1, 24)); // NOI18N
        labelNetworkIp.setForeground(new java.awt.Color(255, 255, 255));
        labelNetworkIp.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelNetworkIp.setText("0.0.0.0");

        labelNetworkDns.setFont(new java.awt.Font("Dialog", 1, 20)); // NOI18N
        labelNetworkDns.setForeground(new java.awt.Color(255, 255, 255));
        labelNetworkDns.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelNetworkDns.setText("0");

        labelNetworkSimboloPorcentagem4.setBackground(new java.awt.Color(102, 102, 102));
        labelNetworkSimboloPorcentagem4.setFont(new java.awt.Font("Dialog", 1, 13)); // NOI18N
        labelNetworkSimboloPorcentagem4.setForeground(new java.awt.Color(107, 107, 107));
        labelNetworkSimboloPorcentagem4.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelNetworkSimboloPorcentagem4.setText("DNS");

        labelNetworkSimboloPorcentagem5.setBackground(new java.awt.Color(102, 102, 102));
        labelNetworkSimboloPorcentagem5.setFont(new java.awt.Font("Dialog", 1, 13)); // NOI18N
        labelNetworkSimboloPorcentagem5.setForeground(new java.awt.Color(107, 107, 107));
        labelNetworkSimboloPorcentagem5.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelNetworkSimboloPorcentagem5.setText("Gateway");

        labelNetworkGateway.setFont(new java.awt.Font("Dialog", 1, 20)); // NOI18N
        labelNetworkGateway.setForeground(new java.awt.Color(255, 255, 255));
        labelNetworkGateway.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelNetworkGateway.setText("0");

        comboBoxNetworkInterfacesName.setBackground(new java.awt.Color(60, 63, 66));
        comboBoxNetworkInterfacesName.setFont(new java.awt.Font("Dialog", 1, 11)); // NOI18N
        comboBoxNetworkInterfacesName.setForeground(new java.awt.Color(255, 255, 255));
        comboBoxNetworkInterfacesName.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "eth0" }));
        comboBoxNetworkInterfacesName.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                OnComboBoxChangeState_NetworkInterfaceName(evt);
            }
        });

        labelNetworkMac.setFont(new java.awt.Font("Dialog", 1, 20)); // NOI18N
        labelNetworkMac.setForeground(new java.awt.Color(255, 255, 255));
        labelNetworkMac.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelNetworkMac.setText("?");

        labelNetworkTextIp1.setFont(new java.awt.Font("Dialog", 1, 13)); // NOI18N
        labelNetworkTextIp1.setForeground(new java.awt.Color(107, 107, 107));
        labelNetworkTextIp1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelNetworkTextIp1.setText("mac");

        javax.swing.GroupLayout panelNetworkLayout = new javax.swing.GroupLayout(panelNetwork);
        panelNetwork.setLayout(panelNetworkLayout);
        panelNetworkLayout.setHorizontalGroup(
            panelNetworkLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelNetworkLayout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(panelNetworkLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelNetworkLayout.createSequentialGroup()
                        .addComponent(imageNetwork, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addGroup(panelNetworkLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(labelNetworkFabricante, javax.swing.GroupLayout.PREFERRED_SIZE, 410, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(panelNetworkLayout.createSequentialGroup()
                                .addComponent(labelNetworkTextNome)
                                .addGap(12, 12, 12)
                                .addComponent(labelNetworkNome, javax.swing.GroupLayout.PREFERRED_SIZE, 355, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(panelNetworkLayout.createSequentialGroup()
                                .addComponent(labelNetworkTextIp)
                                .addGap(6, 6, 6)
                                .addComponent(labelNetworkIp, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(6, 6, 6)
                                .addComponent(labelNetworkTextIp1)
                                .addGap(6, 6, 6)
                                .addComponent(labelNetworkMac, javax.swing.GroupLayout.PREFERRED_SIZE, 167, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(panelNetworkLayout.createSequentialGroup()
                                .addComponent(labelNetworkSimboloPorcentagem4)
                                .addGap(6, 6, 6)
                                .addComponent(labelNetworkDns, javax.swing.GroupLayout.PREFERRED_SIZE, 377, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(panelNetworkLayout.createSequentialGroup()
                        .addComponent(comboBoxNetworkInterfacesName, javax.swing.GroupLayout.PREFERRED_SIZE, 191, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addComponent(labelNetworkSimboloPorcentagem5)
                        .addGap(6, 6, 6)
                        .addComponent(labelNetworkGateway, javax.swing.GroupLayout.PREFERRED_SIZE, 262, javax.swing.GroupLayout.PREFERRED_SIZE))))
        );
        panelNetworkLayout.setVerticalGroup(
            panelNetworkLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelNetworkLayout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(panelNetworkLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(imageNetwork, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(panelNetworkLayout.createSequentialGroup()
                        .addComponent(labelNetworkFabricante)
                        .addGap(4, 4, 4)
                        .addGroup(panelNetworkLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panelNetworkLayout.createSequentialGroup()
                                .addGap(3, 3, 3)
                                .addComponent(labelNetworkTextNome))
                            .addComponent(labelNetworkNome, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(12, 12, 12)
                        .addGroup(panelNetworkLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panelNetworkLayout.createSequentialGroup()
                                .addGap(3, 3, 3)
                                .addComponent(labelNetworkTextIp, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(labelNetworkIp, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(panelNetworkLayout.createSequentialGroup()
                                .addGap(3, 3, 3)
                                .addComponent(labelNetworkTextIp1, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(panelNetworkLayout.createSequentialGroup()
                                .addGap(1, 1, 1)
                                .addComponent(labelNetworkMac, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(6, 6, 6)
                        .addGroup(panelNetworkLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panelNetworkLayout.createSequentialGroup()
                                .addGap(4, 4, 4)
                                .addComponent(labelNetworkSimboloPorcentagem4))
                            .addComponent(labelNetworkDns, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(6, 6, 6)
                .addGroup(panelNetworkLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labelNetworkGateway, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(panelNetworkLayout.createSequentialGroup()
                        .addGap(4, 4, 4)
                        .addGroup(panelNetworkLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(labelNetworkSimboloPorcentagem5)
                            .addComponent(comboBoxNetworkInterfacesName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
        );

        panelMonitoramento.add(panelNetwork, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 100, 535, 160));

        panelOS.setBackground(new java.awt.Color(60, 63, 66));
        panelOS.setPreferredSize(new java.awt.Dimension(356, 130));

        imageOS.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        imageOS.setIcon(new javax.swing.ImageIcon(getClass().getResource("/smartmonkeymonitoramento/images/icon_os_question.png"))); // NOI18N

        labelOSFabricante.setFont(new java.awt.Font("Dialog", 1, 13)); // NOI18N
        labelOSFabricante.setForeground(new java.awt.Color(107, 107, 107));
        labelOSFabricante.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelOSFabricante.setText("<manufacturer>");

        labelOSVersao.setFont(new java.awt.Font("Dialog", 1, 24)); // NOI18N
        labelOSVersao.setForeground(new java.awt.Color(255, 255, 255));
        labelOSVersao.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelOSVersao.setText("0");

        labelOSSimboloPorcentagem.setBackground(new java.awt.Color(102, 102, 102));
        labelOSSimboloPorcentagem.setFont(new java.awt.Font("Dialog", 1, 16)); // NOI18N
        labelOSSimboloPorcentagem.setForeground(new java.awt.Color(107, 107, 107));
        labelOSSimboloPorcentagem.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelOSSimboloPorcentagem.setText("version");

        labelOSTipoSistema.setFont(new java.awt.Font("Dialog", 1, 24)); // NOI18N
        labelOSTipoSistema.setForeground(new java.awt.Color(255, 255, 255));
        labelOSTipoSistema.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelOSTipoSistema.setText("0");

        labelOSTotalProcessos.setFont(new java.awt.Font("Dialog", 1, 20)); // NOI18N
        labelOSTotalProcessos.setForeground(new java.awt.Color(255, 255, 255));
        labelOSTotalProcessos.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelOSTotalProcessos.setText("0");

        labelOSFrequenciaGhz.setFont(new java.awt.Font("Dialog", 1, 13)); // NOI18N
        labelOSFrequenciaGhz.setForeground(new java.awt.Color(107, 107, 107));
        labelOSFrequenciaGhz.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelOSFrequenciaGhz.setText("System Architecture");

        labelOSSimboloPorcentagem1.setBackground(new java.awt.Color(102, 102, 102));
        labelOSSimboloPorcentagem1.setFont(new java.awt.Font("Dialog", 1, 13)); // NOI18N
        labelOSSimboloPorcentagem1.setForeground(new java.awt.Color(107, 107, 107));
        labelOSSimboloPorcentagem1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelOSSimboloPorcentagem1.setText("Active Processes");

        labelOSSimboloPorcentagem2.setBackground(new java.awt.Color(102, 102, 102));
        labelOSSimboloPorcentagem2.setFont(new java.awt.Font("Dialog", 1, 13)); // NOI18N
        labelOSSimboloPorcentagem2.setForeground(new java.awt.Color(107, 107, 107));
        labelOSSimboloPorcentagem2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelOSSimboloPorcentagem2.setText("Active Threads");

        labelOSTotalThreads.setFont(new java.awt.Font("Dialog", 1, 20)); // NOI18N
        labelOSTotalThreads.setForeground(new java.awt.Color(255, 255, 255));
        labelOSTotalThreads.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelOSTotalThreads.setText("0");

        javax.swing.GroupLayout panelOSLayout = new javax.swing.GroupLayout(panelOS);
        panelOS.setLayout(panelOSLayout);
        panelOSLayout.setHorizontalGroup(
            panelOSLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelOSLayout.createSequentialGroup()
                .addGroup(panelOSLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelOSLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(labelOSFabricante, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelOSLayout.createSequentialGroup()
                        .addGap(8, 8, 8)
                        .addComponent(imageOS, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addGroup(panelOSLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panelOSLayout.createSequentialGroup()
                                .addComponent(labelOSFrequenciaGhz, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(labelOSTipoSistema, javax.swing.GroupLayout.PREFERRED_SIZE, 262, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(labelOSSimboloPorcentagem)
                            .addComponent(labelOSVersao, javax.swing.GroupLayout.PREFERRED_SIZE, 410, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(panelOSLayout.createSequentialGroup()
                                .addGap(15, 15, 15)
                                .addComponent(labelOSSimboloPorcentagem1)
                                .addGap(6, 6, 6)
                                .addComponent(labelOSTotalProcessos, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(6, 6, 6)
                                .addComponent(labelOSSimboloPorcentagem2, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(6, 6, 6)
                                .addComponent(labelOSTotalThreads, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap())
        );
        panelOSLayout.setVerticalGroup(
            panelOSLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelOSLayout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addComponent(imageOS, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(panelOSLayout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addComponent(labelOSSimboloPorcentagem)
                .addGap(6, 6, 6)
                .addComponent(labelOSVersao, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(9, 9, 9)
                .addComponent(labelOSFrequenciaGhz, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addGroup(panelOSLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labelOSTotalProcessos, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelOSTotalThreads, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(panelOSLayout.createSequentialGroup()
                        .addGap(4, 4, 4)
                        .addGroup(panelOSLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(labelOSSimboloPorcentagem1)
                            .addComponent(labelOSSimboloPorcentagem2)))))
            .addGroup(panelOSLayout.createSequentialGroup()
                .addGap(61, 61, 61)
                .addComponent(labelOSTipoSistema, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(panelOSLayout.createSequentialGroup()
                .addGap(103, 103, 103)
                .addComponent(labelOSFabricante))
        );

        panelMonitoramento.add(panelOS, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 270, 535, 127));

        panelProcess.setBackground(new java.awt.Color(60, 63, 66));
        panelProcess.setPreferredSize(new java.awt.Dimension(356, 130));

        imageProcess.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        imageProcess.setIcon(new javax.swing.ImageIcon(getClass().getResource("/smartmonkeymonitoramento/images/icon-process-file.png"))); // NOI18N

        labelProcessSimboloPorcentagem3.setBackground(new java.awt.Color(102, 102, 102));
        labelProcessSimboloPorcentagem3.setFont(new java.awt.Font("Dialog", 1, 16)); // NOI18N
        labelProcessSimboloPorcentagem3.setForeground(new java.awt.Color(107, 107, 107));
        labelProcessSimboloPorcentagem3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelProcessSimboloPorcentagem3.setText("TOP 10 PROCESSES");

        labelProcessSimboloPorcentagem5.setBackground(new java.awt.Color(102, 102, 102));
        labelProcessSimboloPorcentagem5.setForeground(new java.awt.Color(107, 107, 107));
        labelProcessSimboloPorcentagem5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelProcessSimboloPorcentagem5.setText("Name");

        labelProcessSimboloPorcentagem6.setBackground(new java.awt.Color(102, 102, 102));
        labelProcessSimboloPorcentagem6.setForeground(new java.awt.Color(107, 107, 107));
        labelProcessSimboloPorcentagem6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelProcessSimboloPorcentagem6.setText("PID");

        labelProcessSimboloPorcentagem7.setBackground(new java.awt.Color(102, 102, 102));
        labelProcessSimboloPorcentagem7.setForeground(new java.awt.Color(107, 107, 107));
        labelProcessSimboloPorcentagem7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelProcessSimboloPorcentagem7.setText("%CPU");

        labelProcessSimboloPorcentagem8.setBackground(new java.awt.Color(102, 102, 102));
        labelProcessSimboloPorcentagem8.setForeground(new java.awt.Color(107, 107, 107));
        labelProcessSimboloPorcentagem8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelProcessSimboloPorcentagem8.setText("RAM");

        labelProcessSimboloPorcentagem9.setBackground(new java.awt.Color(102, 102, 102));
        labelProcessSimboloPorcentagem9.setForeground(new java.awt.Color(107, 107, 107));
        labelProcessSimboloPorcentagem9.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelProcessSimboloPorcentagem9.setText("%RAM");

        labelProcessName.setEditable(false);
        labelProcessName.setBackground(new java.awt.Color(69, 73, 73));
        labelProcessName.setColumns(15);
        labelProcessName.setFont(new java.awt.Font("Dialog", 1, 13)); // NOI18N
        labelProcessName.setForeground(new java.awt.Color(255, 255, 255));
        labelProcessName.setRows(8);
        labelProcessName.setText("loading...");
        labelProcessName.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 6, 2, 6));
        labelProcessName.setDisabledTextColor(new java.awt.Color(153, 153, 154));

        labelProcessPid.setEditable(false);
        labelProcessPid.setBackground(new java.awt.Color(69, 73, 73));
        labelProcessPid.setColumns(10);
        labelProcessPid.setFont(new java.awt.Font("Dialog", 1, 13)); // NOI18N
        labelProcessPid.setForeground(new java.awt.Color(255, 255, 255));
        labelProcessPid.setRows(8);
        labelProcessPid.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 6, 2, 6));

        labelProcessCpuPercent.setEditable(false);
        labelProcessCpuPercent.setBackground(new java.awt.Color(69, 73, 73));
        labelProcessCpuPercent.setColumns(10);
        labelProcessCpuPercent.setFont(new java.awt.Font("Dialog", 1, 13)); // NOI18N
        labelProcessCpuPercent.setForeground(new java.awt.Color(255, 255, 255));
        labelProcessCpuPercent.setRows(8);
        labelProcessCpuPercent.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 6, 2, 6));

        labelProcessRam.setEditable(false);
        labelProcessRam.setBackground(new java.awt.Color(69, 73, 73));
        labelProcessRam.setColumns(11);
        labelProcessRam.setFont(new java.awt.Font("Dialog", 1, 13)); // NOI18N
        labelProcessRam.setForeground(new java.awt.Color(255, 255, 255));
        labelProcessRam.setRows(8);
        labelProcessRam.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 6, 2, 6));

        labelProcessRamPercent.setEditable(false);
        labelProcessRamPercent.setBackground(new java.awt.Color(69, 73, 73));
        labelProcessRamPercent.setColumns(9);
        labelProcessRamPercent.setFont(new java.awt.Font("Dialog", 1, 13)); // NOI18N
        labelProcessRamPercent.setForeground(new java.awt.Color(255, 255, 255));
        labelProcessRamPercent.setRows(8);
        labelProcessRamPercent.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 6, 2, 6));

        javax.swing.GroupLayout panelProcessLayout = new javax.swing.GroupLayout(panelProcess);
        panelProcess.setLayout(panelProcessLayout);
        panelProcessLayout.setHorizontalGroup(
            panelProcessLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelProcessLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addComponent(imageProcess, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addGroup(panelProcessLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labelProcessSimboloPorcentagem3, javax.swing.GroupLayout.PREFERRED_SIZE, 434, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(panelProcessLayout.createSequentialGroup()
                        .addComponent(labelProcessSimboloPorcentagem5, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addComponent(labelProcessSimboloPorcentagem6, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addComponent(labelProcessSimboloPorcentagem7, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addComponent(labelProcessSimboloPorcentagem8, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addComponent(labelProcessSimboloPorcentagem9, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelProcessLayout.createSequentialGroup()
                        .addComponent(labelProcessName, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addComponent(labelProcessPid, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addComponent(labelProcessCpuPercent, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addComponent(labelProcessRam, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addComponent(labelProcessRamPercent, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE))))
        );
        panelProcessLayout.setVerticalGroup(
            panelProcessLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelProcessLayout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(panelProcessLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(imageProcess, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(panelProcessLayout.createSequentialGroup()
                        .addComponent(labelProcessSimboloPorcentagem3)
                        .addGap(6, 6, 6)
                        .addGroup(panelProcessLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(labelProcessSimboloPorcentagem5)
                            .addComponent(labelProcessSimboloPorcentagem6)
                            .addComponent(labelProcessSimboloPorcentagem7)
                            .addComponent(labelProcessSimboloPorcentagem8)
                            .addComponent(labelProcessSimboloPorcentagem9))
                        .addGap(6, 6, 6)
                        .addGroup(panelProcessLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(labelProcessName, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(labelProcessPid, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(labelProcessCpuPercent, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(labelProcessRam, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(labelProcessRamPercent, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE)))))
        );

        panelMonitoramento.add(panelProcess, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 410, 535, 247));

        labelLogUrl.setFont(new java.awt.Font("Dialog", 3, 13)); // NOI18N
        labelLogUrl.setForeground(new java.awt.Color(102, 102, 102));
        labelLogUrl.setText("?");
        panelMonitoramento.add(labelLogUrl, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 570, 350, -1));

        labelLogUrlTitle.setFont(new java.awt.Font("Dialog", 3, 13)); // NOI18N
        labelLogUrlTitle.setForeground(new java.awt.Color(102, 102, 102));
        labelLogUrlTitle.setText("Log Path:");
        panelMonitoramento.add(labelLogUrlTitle, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 570, -1, -1));

        labelCurrentUpdate.setFont(new java.awt.Font("Dialog", 3, 14)); // NOI18N
        labelCurrentUpdate.setForeground(new java.awt.Color(102, 102, 102));
        labelCurrentUpdate.setText("0");
        panelMonitoramento.add(labelCurrentUpdate, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 640, 101, -1));

        buttonGravarDados.setBackground(new java.awt.Color(154, 47, 69));
        buttonGravarDados.setFont(new java.awt.Font("Dialog", 1, 13)); // NOI18N
        buttonGravarDados.setForeground(new java.awt.Color(187, 187, 186));
        buttonGravarDados.setIcon(new javax.swing.ImageIcon(getClass().getResource("/smartmonkeymonitoramento/images/rec-icon.png"))); // NOI18N
        buttonGravarDados.setText("Write Data to Database");
        buttonGravarDados.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonGravarDadosActionPerformed(evt);
            }
        });
        panelMonitoramento.add(buttonGravarDados, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 590, -1, 46));

        labelTextCurrenteUpdate1.setFont(new java.awt.Font("Dialog", 3, 13)); // NOI18N
        labelTextCurrenteUpdate1.setForeground(new java.awt.Color(102, 102, 102));
        labelTextCurrenteUpdate1.setText("CountUpdate:");
        panelMonitoramento.add(labelTextCurrenteUpdate1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 640, -1, -1));

        labelDelimitsTitle.setFont(new java.awt.Font("Dialog", 3, 13)); // NOI18N
        labelDelimitsTitle.setForeground(new java.awt.Color(102, 102, 102));
        labelDelimitsTitle.setText("Delimits:");
        panelMonitoramento.add(labelDelimitsTitle, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 550, -1, -1));

        labelDelimits.setFont(new java.awt.Font("Dialog", 3, 13)); // NOI18N
        labelDelimits.setForeground(new java.awt.Color(102, 102, 102));
        labelDelimits.setText("?");
        panelMonitoramento.add(labelDelimits, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 550, 380, -1));

        jTabbedPane1.addTab("Monitoring", panelMonitoramento);

        panelSobreNos.setBackground(new java.awt.Color(51, 51, 51));
        panelSobreNos.setMaximumSize(null);

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 48)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(255, 255, 255));
        jLabel6.setText("Smart Monkey Team");

        jLabel7.setFont(new java.awt.Font("Lucida Sans Typewriter", 0, 18)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(255, 255, 255));
        jLabel7.setText("Allan Tavares - C# Website");

        jLabel8.setFont(new java.awt.Font("Lucida Sans Typewriter", 0, 18)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(255, 255, 255));
        jLabel8.setText("Caio Cipresso - BI with QlikSense");

        jLabel9.setFont(new java.awt.Font("Lucida Sans Typewriter", 0, 18)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(255, 255, 255));
        jLabel9.setText("Juliana Oliveira - BI with QlikSense");

        jLabel10.setFont(new java.awt.Font("Lucida Sans Typewriter", 0, 18)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(255, 255, 255));
        jLabel10.setText("https://smartmonkeymonitoring.azurewebsites.net");

        jLabel11.setFont(new java.awt.Font("Lucida Sans Typewriter", 0, 18)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(255, 255, 255));
        jLabel11.setText("Tiago Pedroso - Java");

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/smartmonkeymonitoramento/images/logo-smartmonkey-white-full.png"))); // NOI18N

        javax.swing.GroupLayout panelSobreNosLayout = new javax.swing.GroupLayout(panelSobreNos);
        panelSobreNos.setLayout(panelSobreNosLayout);
        panelSobreNosLayout.setHorizontalGroup(
            panelSobreNosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelSobreNosLayout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addComponent(jLabel6)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelSobreNosLayout.createSequentialGroup()
                .addGap(158, 158, 158)
                .addGroup(panelSobreNosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelSobreNosLayout.createSequentialGroup()
                        .addGap(15, 15, 15)
                        .addGroup(panelSobreNosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel8)
                            .addComponent(jLabel7)))
                    .addComponent(jLabel9)
                    .addGroup(panelSobreNosLayout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addComponent(jLabel11)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(panelSobreNosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel10)
                    .addComponent(jLabel1))
                .addGap(67, 67, 67))
        );
        panelSobreNosLayout.setVerticalGroup(
            panelSobreNosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelSobreNosLayout.createSequentialGroup()
                .addGroup(panelSobreNosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelSobreNosLayout.createSequentialGroup()
                        .addGap(43, 43, 43)
                        .addComponent(jLabel6)
                        .addGap(30, 30, 30)
                        .addComponent(jLabel7)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel8)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel11))
                    .addGroup(panelSobreNosLayout.createSequentialGroup()
                        .addContainerGap(192, Short.MAX_VALUE)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 432, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel10)
                .addGap(15, 15, 15))
        );

        jTabbedPane1.addTab("About Us", panelSobreNos);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 1014, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    //</editor-fold>

    private void OnComboBoxChangeState_NetworkInterfaceName(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_OnComboBoxChangeState_NetworkInterfaceName
        indexOfCurrentNetworkInterface = comboBoxNetworkInterfacesName.getSelectedIndex();
    }//GEN-LAST:event_OnComboBoxChangeState_NetworkInterfaceName

    private void buttonGravarDadosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonGravarDadosActionPerformed
        updateButtonGravarDados();
    }//GEN-LAST:event_buttonGravarDadosActionPerformed

    // <editor-fold defaultstate="collapsed" desc="Events">
    private void OnItemStateChanged_ComboBoxSelectDisk(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_OnItemStateChanged_ComboBoxSelectDisk
        indexOfCurrentDisk = comboBoxSelectDisk.getSelectedIndex();
    }//GEN-LAST:event_OnItemStateChanged_ComboBoxSelectDisk

    private void comboBoxHostIdOnComboBoxChangeState_HostId(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_comboBoxHostIdOnComboBoxChangeState_HostId
        updateIndexOfSelectedMachine();
    }//GEN-LAST:event_comboBoxHostIdOnComboBoxChangeState_HostId

    private void OnWindowCloese(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_OnWindowCloese
        System.exit(0);
    }//GEN-LAST:event_OnWindowCloese

    //<editor-fold defaultstate="collapsed" desc="methods: computer screen resolution...">
    private static Integer getScreenWidth() {
        return Toolkit.getDefaultToolkit().getScreenSize().width;
    }

    private static Integer getScreenHeight() {
        return Toolkit.getDefaultToolkit().getScreenSize().height;
    }

    private void moveJFrameToCenterOfScreen() {
        super.setLocation((getScreenWidth() / 2) - (super.getWidth() / 2),
                (getScreenHeight() / 2) - (super.getHeight() / 2)
        );
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="method: main(String args[])">
    public static void main(String args[]) {
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
            java.util.logging.Logger.getLogger(LocalMonitoring.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(LocalMonitoring.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(LocalMonitoring.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(LocalMonitoring.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            new LocalMonitoring().setVisible(true);
        });
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="generated attributes">
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton buttonGravarDados;
    private javax.swing.JComboBox<String> comboBoxHostId;
    private javax.swing.JComboBox<String> comboBoxNetworkInterfacesName;
    private javax.swing.JComboBox<String> comboBoxSelectDisk;
    private javax.swing.JLabel imageCpu;
    private javax.swing.JLabel imageDisk;
    private javax.swing.JLabel imageLogoMonitoramento;
    private javax.swing.JLabel imageNetwork;
    private javax.swing.JLabel imageOS;
    private javax.swing.JLabel imageProcess;
    private javax.swing.JLabel imageRam;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel labelCpuCoresQtd;
    private javax.swing.JLabel labelCpuCoresText;
    private javax.swing.JLabel labelCpuFabricante;
    private javax.swing.JLabel labelCpuFrequencia;
    private javax.swing.JLabel labelCpuFrequenciaGhz;
    private javax.swing.JLabel labelCpuModelo;
    private javax.swing.JLabel labelCpuSimboloPorcentagem;
    private javax.swing.JLabel labelCpuUso;
    private javax.swing.JLabel labelCurrentUpdate;
    private javax.swing.JLabel labelDelimits;
    private javax.swing.JLabel labelDelimitsTitle;
    private javax.swing.JLabel labelDiskCoresText;
    private javax.swing.JLabel labelDiskFabricante;
    private javax.swing.JLabel labelDiskFrequenciaGhz;
    private javax.swing.JLabel labelDiskSimboloBarra;
    private javax.swing.JLabel labelDiskSimboloPorcentagem;
    private javax.swing.JLabel labelDiskTotalGb;
    private javax.swing.JLabel labelDiskUso;
    private javax.swing.JLabel labelDiskUsoGb;
    private javax.swing.JLabel labelHostId1;
    private javax.swing.JLabel labelLogUrl;
    private javax.swing.JLabel labelLogUrlTitle;
    private javax.swing.JLabel labelMonitoramentoPC;
    private javax.swing.JLabel labelMonitoramentoPC1;
    private javax.swing.JLabel labelNetworkDns;
    private javax.swing.JLabel labelNetworkFabricante;
    private javax.swing.JLabel labelNetworkGateway;
    private javax.swing.JLabel labelNetworkIp;
    private javax.swing.JLabel labelNetworkMac;
    private javax.swing.JLabel labelNetworkNome;
    private javax.swing.JLabel labelNetworkSimboloPorcentagem4;
    private javax.swing.JLabel labelNetworkSimboloPorcentagem5;
    private javax.swing.JLabel labelNetworkTextIp;
    private javax.swing.JLabel labelNetworkTextIp1;
    private javax.swing.JLabel labelNetworkTextNome;
    private javax.swing.JLabel labelOSFabricante;
    private javax.swing.JLabel labelOSFrequenciaGhz;
    private javax.swing.JLabel labelOSSimboloPorcentagem;
    private javax.swing.JLabel labelOSSimboloPorcentagem1;
    private javax.swing.JLabel labelOSSimboloPorcentagem2;
    private javax.swing.JLabel labelOSTipoSistema;
    private javax.swing.JLabel labelOSTotalProcessos;
    private javax.swing.JLabel labelOSTotalThreads;
    private javax.swing.JLabel labelOSVersao;
    private javax.swing.JTextArea labelProcessCpuPercent;
    private javax.swing.JTextArea labelProcessName;
    private javax.swing.JTextArea labelProcessPid;
    private javax.swing.JTextArea labelProcessRam;
    private javax.swing.JTextArea labelProcessRamPercent;
    private javax.swing.JLabel labelProcessSimboloPorcentagem3;
    private javax.swing.JLabel labelProcessSimboloPorcentagem5;
    private javax.swing.JLabel labelProcessSimboloPorcentagem6;
    private javax.swing.JLabel labelProcessSimboloPorcentagem7;
    private javax.swing.JLabel labelProcessSimboloPorcentagem8;
    private javax.swing.JLabel labelProcessSimboloPorcentagem9;
    private javax.swing.JLabel labelRamCoresText;
    private javax.swing.JLabel labelRamCoresText1;
    private javax.swing.JLabel labelRamCoresText2;
    private javax.swing.JLabel labelRamFabricante;
    private javax.swing.JLabel labelRamFrequenciaGhz;
    private javax.swing.JLabel labelRamFrequenciaGhz1;
    private javax.swing.JLabel labelRamSimboloBarra;
    private javax.swing.JLabel labelRamSimboloPorcentagem;
    private javax.swing.JLabel labelRamTotalGb;
    private javax.swing.JLabel labelRamUso;
    private javax.swing.JLabel labelRamUsoGb;
    private javax.swing.JLabel labelRamVirtualTotalGb;
    private javax.swing.JLabel labelRamVirtualUsoGb;
    private javax.swing.JLabel labelTextCurrenteUpdate1;
    private javax.swing.JPanel painelAlerta;
    private javax.swing.JPanel painelSuperior;
    private javax.swing.JPanel panelCpu;
    private javax.swing.JPanel panelDisk;
    private javax.swing.JPanel panelMonitoramento;
    private javax.swing.JPanel panelNetwork;
    private javax.swing.JPanel panelOS;
    private javax.swing.JPanel panelProcess;
    private javax.swing.JPanel panelRam;
    private javax.swing.JPanel panelSobreNos;
    // End of variables declaration//GEN-END:variables
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="auxiliary attributes">
    private int countDebugUpdate = 0;
    private String temp;
    private String tempName;
    private String tempPid;
    private String tempCpuPercent;
    private String tempRam;
    private String tempRamPercent;
    //</editor-fold>

}
