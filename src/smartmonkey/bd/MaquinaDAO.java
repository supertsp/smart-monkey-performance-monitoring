package smartmonkey.bd;

import java.util.List;
import smartdb.SmartDbConnection;
import static smartmonkey.bd.ConexaoSqlServer.iniciarConexao;

/**
 *
 * @author tiago penha pedroso, 2019-06-08
 */
public class MaquinaDAO {

    public static List<Maquina> getAll() {
        iniciarConexao();
        
        List<Maquina> lista = SmartDbConnection.executeQueryToReturnList(Maquina.class,
                "SELECT * FROM Maquina"
        );
        return lista;
    }

    public static Integer getAvaliableId() {
        iniciarConexao();
                
        Integer idAvaliable = SmartDbConnection.executeQueryToReturnObject(Integer.class,
                    "SELECT ISNULL(MAX(idMaquina) + 1, 0) FROM Maquina"
        );
        return idAvaliable;
    }

    public static Maquina get(int idProcurado) {        
        iniciarConexao();
        
        Maquina registro = SmartDbConnection.executeQueryToReturnMappedObject(Maquina.class,
                "SELECT * FROM Maquina where idMaquina = ?",
                idProcurado
        );

        return registro;
    }

    public static void add(Maquina novoRegistro) {
        iniciarConexao();
        
        SmartDbConnection.executeQuery(
            "INSERT INTO Maquina (modelo, processador, memoriaRam, discoRigido, delimitCpu, delimitRam, delimitHd) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?)",
            novoRegistro.getModelo(),
            novoRegistro.getProcessador(),
            novoRegistro.getMemoriaRam(),
            novoRegistro.getDiscoRigido(),
            novoRegistro.getDelimitCpu(),
            novoRegistro.getDelimitRam(),
            novoRegistro.getDelimitHd()
        );
    }

    public static void addWithFk(Maquina novoRegistro) {
        iniciarConexao();
        
        SmartDbConnection.executeQuery(
                "INSERT INTO Maquina (modelo, processador, memoriaRam, discoRigido, delimitCpu, delimitRam, delimitHd, idInstituicao) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                novoRegistro.getModelo(),
                novoRegistro.getProcessador(),
                novoRegistro.getMemoriaRam(),
                novoRegistro.getDiscoRigido(),
                novoRegistro.getDelimitCpu(),
                novoRegistro.getDelimitRam(),
                novoRegistro.getDelimitHd(),
                novoRegistro.getIdInstituicao()
        );
    }

    public static void remove(int idProcurado) {
        iniciarConexao();
        
        SmartDbConnection.executeQuery(
                "DELETE FROM Maquina WHERE idMaquina = " + idProcurado
        );
    }

    public static void update(int idProcurado, Maquina updateRegistro) {
        iniciarConexao();
        
        SmartDbConnection.executeQuery(
                "UPDATE Maquina SET\n"
                + "modelo = ?,\n"
                + "processador = ?,\n"
                + "memoriaRam = ?,\n"
                + "discoRigido = ?,\n"
                + "delimitCpu = ?,\n"
                + "delimitRam = ?,\n"
                + "delimitHd = ?\n"
                + "WHERE idMaquina = ?",
                updateRegistro.getModelo(),
                updateRegistro.getProcessador(),
                updateRegistro.getMemoriaRam(),
                updateRegistro.getDiscoRigido(),
                updateRegistro.getDelimitCpu(),
                updateRegistro.getDelimitRam(),
                updateRegistro.getDelimitHd(),
                idProcurado
        );
    }

    public static void updateWithFk(int idProcurado, Maquina updateRegistro) {
        iniciarConexao();
        
        SmartDbConnection.executeQuery(
                "UPDATE Maquina SET\n"
                + "modelo = ?,\n"
                + "processador = ?,\n"
                + "memoriaRam = ?,\n"
                + "discoRigido = ?,\n"
                + "delimitCpu = ?,\n"
                + "delimitRam = ?,\n"
                + "delimitHd = ?,\n"
                + "idInstituicao = ?\n"
                + "WHERE idMaquina = ?",
                updateRegistro.getModelo(),
                updateRegistro.getProcessador(),
                updateRegistro.getMemoriaRam(),
                updateRegistro.getDiscoRigido(),
                updateRegistro.getDelimitCpu(),
                updateRegistro.getDelimitRam(),
                updateRegistro.getDelimitHd(),
                updateRegistro.getIdInstituicao(),
                idProcurado
        );
    }

}
