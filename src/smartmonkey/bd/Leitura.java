package smartmonkey.bd;

import java.util.Calendar;

/**
 *
 * @author tiago penha pedroso, 2019-06-08
 */
public class Leitura {

    private Integer idLeitura = -1;
    private Double porcProcessador;
    private Double porcRam;
    private Double porcHd;
    private String dataLeitura;
    private Integer idMaquina = -1;

    // <editor-fold defaultstate="collapsed" desc="Construtores"> 
    public Leitura() {
        //vazio para o Spring JDBC retornar uma lista completa (SELECT * FROM)
    }

    public Leitura(Double porcProcessador, Double porcRam, Double porcHd) {
        this();
        this.porcProcessador = porcProcessador;
        this.porcRam = porcRam;
        this.porcHd = porcHd;
        setDataLeituraAleatoria();
    }

    public Leitura(Double porcProcessador, Double porcRam, Double porcHd, String dataLeitura) {
        this();
        this.porcProcessador = porcProcessador;
        this.porcRam = porcRam;
        this.porcHd = porcHd;
        this.dataLeitura = dataLeitura;
    }

    public Leitura(Integer idLeitura, Double porcProcessador, Double porcRam, Double porcHd, String dataLeitura) {
        this();
        this.idLeitura = idLeitura;
        this.porcProcessador = porcProcessador;
        this.porcRam = porcRam;
        this.porcHd = porcHd;
        this.dataLeitura = dataLeitura;
    }

    public Leitura(Double porcProcessador, Double porcRam, Double porcHd, String dataLeitura, Integer idMaquina) {
        this(porcProcessador, porcRam, porcHd, dataLeitura);
        this.idMaquina = idMaquina;
    }

    public Leitura(Double porcProcessador, Double porcRam, Double porcHd, Integer idMaquina) {
        this(porcProcessador, porcRam, porcHd);
        this.idMaquina = idMaquina;
    }
    //</editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Getters & Setters"> 
    public Integer getIdLeitura() {
        return idLeitura;
    }

    public void setIdLeitura(Integer idLeitura) {
        this.idLeitura = idLeitura;
    }

    public Double getPorcProcessador() {
        return porcProcessador;
    }

    public void setPorcProcessador(Double porcProcessador) {
        this.porcProcessador = porcProcessador;
    }

    public Double getPorcRam() {
        return porcRam;
    }

    public void setPorcRam(Double porcRam) {
        this.porcRam = porcRam;
    }

    public Double getPorcHd() {
        return porcHd;
    }

    public void setPorcHd(Double porcHd) {
        this.porcHd = porcHd;
    }

    public String getDataLeitura() {
        return dataLeitura;
    }

    public void setDataLeitura(String dataLeitura) {
        this.dataLeitura = dataLeitura;
    }

    public void setDataLeitura(int dia, int mes, int ano) {
        dataLeitura = "" + ano + "-" + mes + "-" + dia;
    }

    public void setDataLeitura(int dia, int mes, int ano, int hora, int minuto, int segundo, int milesimo) {
        dataLeitura = "" + ano + "-" + mes + "-" + dia + " " + hora + ":" + minuto + ":" + segundo + "." + milesimo;
    }

    public void setDataLeituraAleatoria() {
        setDataLeitura(
                ((int) (Math.random() * 28)) + 1,
                ((int) (Math.random() * 12)) + 1,
                Calendar.getInstance().get(Calendar.YEAR)
        );
    }
    
    public void setDataLeituraAleatoria(int ano) {
        setDataLeitura(
                ((int) (Math.random() * 28)) + 1,
                ((int) (Math.random() * 12)) + 1,
                ano
        );
    }
    
    public void setDataLeituraAleatoria(int mesLimiteSorteio, int ano) {
        setDataLeitura(
                ((int) (Math.random() * 28)) + 1,
                ((int) (Math.random() * mesLimiteSorteio)) + 1,
                ano
        );
    }

    public Integer getIdMaquina() {
        return idMaquina;
    }

    public void setIdMaquina(Integer idMaquina) {
        this.idMaquina = idMaquina;
    }
    //</editor-fold>

    @Override
    public String toString() {
        return "\n[" + idLeitura + "(id), " + porcProcessador + " (cpu), "
                + porcRam + " (ram), " + porcHd + " (hd), " + dataLeitura + ", " + idMaquina + " (fk)]\n";
    }

    // <editor-fold defaultstate="collapsed" desc="BD"> 
    public void salvarDados() {
        //salvando pela primeira vez?
        if (idLeitura == -1) {
            idLeitura = LeituraDAO.getAvaliableId();

            if (idMaquina != -1) {
                LeituraDAO.addWithFk(this);
            } else {
                LeituraDAO.add(this);
            }

        } else {
            if (idMaquina != -1) {
                LeituraDAO.updateWithFk(idLeitura, this);
            } else {
                LeituraDAO.update(idLeitura, this);
            }
        }
    }

    public void removerDados() {
        if (idLeitura != -1) {
            LeituraDAO.remove(idLeitura);
            idLeitura = -1;
            idMaquina = -1;
        }
    }
    //</editor-fold>

}
