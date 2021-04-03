package smartmonkey.bd;

import smartdb.SmartDbConnection;

/**
 *
 * @author tiago penha pedroso, 2019-06-08
 */
public class ConexaoSqlServer {
 
    public static void iniciarConexao(){
        if (SmartDbConnection.getJdbcTemplate() == null) {
            SmartDbConnection.setVariablesToSqlServer(
                "jdbc:sqlserver://smartmonkeymonitoring.database.windows.net:1433;database=SmartMonkey;user=admsmart@smartmonkeymonitoring;password={your_password_here};encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;", 
                "admsmart", 
                "MonkeysBusiness02"
            );
            SmartDbConnection.setCurrentDbType(SmartDbConnection.DbType.SQLServer);
        }
    }
    
}
