package endless.utils;

public class UnsupportedFileExtensionException extends RuntimeException{

    private static final long serialVersionUID = 1426560502927910379L;
    
    public UnsupportedFileExtensionException(){
        super();
    }
    
    public UnsupportedFileExtensionException(String message){
        super(message);
    }
    
    public UnsupportedFileExtensionException(String message,Throwable cause){
        super(message,cause);
    }
    
}
