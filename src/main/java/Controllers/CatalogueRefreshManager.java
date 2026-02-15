package Controllers;

public class CatalogueRefreshManager {
    private static CatalogueRefreshManager instance;
    private boolean refreshRequested = false;
    
    private CatalogueRefreshManager() {}
    
    public static CatalogueRefreshManager getInstance() {
        if (instance == null) {
            instance = new CatalogueRefreshManager();
        }
        return instance;
    }
    
    public void requestRefresh() {
        refreshRequested = true;
        notifyAll();
    }
    
    public boolean isRefreshRequested() {
        return refreshRequested;
    }
    
    public void resetRefresh() {
        refreshRequested = false;
    }
}
