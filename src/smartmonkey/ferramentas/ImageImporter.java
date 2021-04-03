package smartmonkey.ferramentas;

import java.awt.Image;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import oshi.PlatformEnum;
import oshi.SimpleSystemInfo;

/**
 *
 * @author tiago penha pedroso, 2019-06-08
 */
public class ImageImporter {
    
    public static Icon logoLandscape = importarImagemBitmap("/smartmonkeymonitoramento/images/logo-smartmonkey-white-landscape_small.png");
    public static Icon iconCpu = importarImagemBitmap("/smartmonkeymonitoramento/images/icon_cpu.png");
    public static Icon iconOs = inicializarIconOs();
    
    
    //ImageImporter.iconCpu
    
    private static ImageImporter instance = new ImageImporter();
        
    
    public static Icon importarImagemBitmap(String url) {
        if (instance == null) {
            instance = new ImageImporter();
        }
        
        return instance.importar(url);
    }
    
    private Icon importar(String url) {
        if (!url.equals("")) {
            try {
                Image imagem = ImageIO.read(getClass().getResource(url));
                ImageIcon icon = new ImageIcon(imagem);
                return icon;

            } catch (Exception e) {
                System.out.println(
                        "+------------------------------------+\n"
                        + "| Erro ao importar a ImagemBitmap! :(\n"
                        + "| URL: " + url + "\n"
                        + "+------------------------------------+\n"
                );
            }
        }

        return null;
    }
    
    private static Icon inicializarIconOs(){
        PlatformEnum platformOs = SimpleSystemInfo.getPlatformOs();
        switch (platformOs) {
            case WINDOWS:
                return importarImagemBitmap("/smartmonkeymonitoramento/images/icon_os_windows.png");
                
            case LINUX:
                return importarImagemBitmap("/smartmonkeymonitoramento/images/icon_os_linux.png");
                
            case MACOSX:
                return importarImagemBitmap("/smartmonkeymonitoramento/images/icon_os_mac.png");
                
            default:
                return importarImagemBitmap("/smartmonkeymonitoramento/images/icon_os_question.png");
        }
        
    }
    
}
