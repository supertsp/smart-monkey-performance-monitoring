package smartmonkey.bd;

import smartdb.SmartDbConnection;

/**
 *
 * @author tiago penha pedroso, 2019-06-08
 */
public class Maquina {

    private Integer idMaquina = -1;
    private String modelo;
    private String processador;
    private Integer memoriaRam;
    private Double discoRigido;
    private Double delimitCpu;
    private Double delimitRam;
    private Double delimitHd;
    private Integer idInstituicao = -1;

    // <editor-fold defaultstate="collapsed" desc="Construtores"> 
    public Maquina() {
        SmartDbConnection.setVariablesToSqlServer(
            "jdbc:sqlserver://smartmonkeymonitoring.database.windows.net:1433;database=SmartMonkey;user=admsmart@smartmonkeymonitoring;password={your_password_here};encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;", 
            "admsmart", 
            "MonkeysBusiness02"
        );
        //vazio para o Spring JDBC retornar uma lista completa (SELECT * FROM)
    }

    public Maquina(String modelo, String processador, Integer memoriaRam, Double discoRigido, Double delimitCpu, Double delimitRam, Double delimitHd) {
        this();
        this.modelo = modelo;
        this.processador = processador;
        this.memoriaRam = memoriaRam;
        this.discoRigido = discoRigido;
        this.delimitCpu = delimitCpu;
        this.delimitRam = delimitRam;
        this.delimitHd = delimitHd;
    }

    public Maquina(Integer idMaquina, String modelo, String processador, Integer memoriaRam, Double discoRigido, Double delimitCpu, Double delimitRam, Double delimitHd) {
        this();
        this.idMaquina = idMaquina;
        this.modelo = modelo;
        this.processador = processador;
        this.memoriaRam = memoriaRam;
        this.discoRigido = discoRigido;
        this.delimitCpu = delimitCpu;
        this.delimitRam = delimitRam;
        this.delimitHd = delimitHd;
    }

    public Maquina(String modelo, String processador, Integer memoriaRam, Double discoRigido, Double delimitCpu, Double delimitRam, Double delimitHd, Integer idInstituicao) {
        this(modelo, processador, memoriaRam, discoRigido, delimitCpu, delimitRam, delimitHd);
        this.idInstituicao = idInstituicao;
    }

    //</editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Getters & Setters"> 
    public Integer getIdMaquina() {
        return idMaquina;
    }

    public void setIdMaquina(Integer idMaquina) {
        this.idMaquina = idMaquina;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getProcessador() {
        return processador;
    }

    public void setProcessador(String processador) {
        this.processador = processador;
    }

    public Integer getMemoriaRam() {
        return memoriaRam;
    }

    public void setMemoriaRam(Integer memoriaRam) {
        this.memoriaRam = memoriaRam;
    }

    public Double getDiscoRigido() {
        return discoRigido;
    }

    public void setDiscoRigido(Double discoRigido) {
        this.discoRigido = discoRigido;
    }

    public Double getDelimitCpu() {
        return delimitCpu;
    }

    public void setDelimitCpu(Double delimitCpu) {
        this.delimitCpu = delimitCpu;
    }

    public Double getDelimitRam() {
        return delimitRam;
    }

    public void setDelimitRam(Double delimitRam) {
        this.delimitRam = delimitRam;
    }

    public Double getDelimitHd() {
        return delimitHd;
    }

    public void setDelimitHd(Double delimitHd) {
        this.delimitHd = delimitHd;
    }

    public Integer getIdInstituicao() {
        return idInstituicao;
    }

    public void setIdInstituicao(Integer idInstituicao) {
        this.idInstituicao = idInstituicao;
    }
    //</editor-fold>

    @Override
    public String toString() {
        return "\n[" + idMaquina + " (id), " + modelo + " (mod), " + processador + " (cpu), "
                + memoriaRam + " (ram), " + discoRigido + " (hd), " + delimitCpu + " (dcpu), "
                + delimitRam + " (dram), " + delimitHd + " (dhd), " + idInstituicao + " (fk)]\n";
    }

    // <editor-fold defaultstate="collapsed" desc="BD"> 
    public void salvarDados() {
        //salvando pela primeira vez?
        if (idMaquina == -1) {
            idMaquina = MaquinaDAO.getAvaliableId();

            if (idInstituicao != -1) {
                MaquinaDAO.addWithFk(this);
            } else {
                MaquinaDAO.add(this);
            }
        } else {
            if (idInstituicao != -1) {
                MaquinaDAO.updateWithFk(idMaquina, this);
            } else {
                MaquinaDAO.update(idMaquina, this);
            }
        }
    }

    public void removerDados() {
        if (idMaquina != -1) {
            MaquinaDAO.remove(idMaquina);
            idMaquina = -1;
            idInstituicao = -1;
        }
    }
    //</editor-fold>

}
