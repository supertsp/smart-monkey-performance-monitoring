package smartmonkey.bd;

import java.util.List;
import smartdb.SmartDbConnection;
import static smartmonkey.bd.ConexaoSqlServer.iniciarConexao;

/**
 *
 * @author tiago penha pedroso, 2019-06-08
 */
public class LeituraDAO {
    
    public static List<Leitura> getAll() {
        iniciarConexao();
        
        List<Leitura> lista = SmartDbConnection.executeQueryToReturnList(Leitura.class,
                "SELECT * FROM Leitura"
        );
        return lista;
    }

    public static Integer getAvaliableId() {        
        iniciarConexao();
        
        Integer idAvaliable = SmartDbConnection.executeQueryToReturnObject(Integer.class,
                "SELECT ISNULL(MAX(idLeitura) + 1, 0) FROM Leitura"
        );
        return idAvaliable;
    }

    public static Leitura get(int idProcurado) {        
        iniciarConexao();
        
        Leitura registro = SmartDbConnection.executeQueryToReturnMappedObject(Leitura.class,
                "SELECT * FROM Leitura where idLeitura = ?",
                idProcurado
        );

        return registro;
    }

    public static void add(Leitura novoRegistro) {        
        iniciarConexao();
        
        SmartDbConnection.executeQuery(
                "INSERT INTO Leitura (porcProcessador, porcRam, porcHd, dataLeitura) " +
                "VALUES (?, ?, ?, ?)",
                novoRegistro.getPorcProcessador(),
                novoRegistro.getPorcRam(),
                novoRegistro.getPorcHd(),
                novoRegistro.getDataLeitura()
        );
    }

    public static void addWithFk(Leitura novoRegistro) {
        iniciarConexao();
        
        SmartDbConnection.executeQuery(
                "INSERT INTO Leitura (porcProcessador, porcRam, porcHd, dataLeitura, idMaquina) " +
                "VALUES (?, ?, ?, ?, ?)",
                novoRegistro.getPorcProcessador(),
                novoRegistro.getPorcRam(),
                novoRegistro.getPorcHd(),
                novoRegistro.getDataLeitura(),
                novoRegistro.getIdMaquina()
        );
    }

    public static void remove(int idProcurado) {        
        iniciarConexao();
        
        SmartDbConnection.executeQuery(
                "DELETE FROM Leitura WHERE idLeitura = " + idProcurado
        );
    }

    public static void update(int idProcurado, Leitura updateRegistro) {
        iniciarConexao();
        
        SmartDbConnection.executeQuery(
                "UPDATE Leitura SET\n"
                + "porcProcessador = ?,\n"
                + "porcRam = ?,\n"
                + "porcHd = ?,\n"
                + "dataLeitura = ?\n"
                + "WHERE idLeitura = ?",
                updateRegistro.getPorcProcessador(),
                updateRegistro.getPorcRam(),
                updateRegistro.getPorcHd(),
                updateRegistro.getDataLeitura(),
                idProcurado
        );
    }

    public static void updateWithFk(int idProcurado, Leitura updateRegistro) {
        iniciarConexao();
        
        SmartDbConnection.executeQuery(
                "UPDATE Leitura SET\n"
                + "porcProcessador = ?,\n"
                + "porcRam = ?,\n"
                + "porcHd = ?,\n"
                + "dataLeitura = ?,\n"
                + "idMaquina = ?\n"
                + "WHERE idLeitura = ?",
                updateRegistro.getPorcProcessador(),
                updateRegistro.getPorcRam(),
                updateRegistro.getPorcHd(),
                updateRegistro.getDataLeitura(),
                updateRegistro.getIdMaquina(),
                idProcurado
        );
    }

}
