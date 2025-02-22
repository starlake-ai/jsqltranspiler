package ai.starlake.transpiler;

public class CatalogNotFoundException extends RuntimeException {
  String catalogName;

  public CatalogNotFoundException(String catalogName, Throwable cause) {
    super("Catalog not found: " + catalogName, cause);
    this.catalogName = catalogName;
  }

  public CatalogNotFoundException(String catalogName) {
    super("Catalog not found: " + catalogName);
    this.catalogName = catalogName;
  }

  public String getCatalogName() {
    return catalogName;
  }
}
