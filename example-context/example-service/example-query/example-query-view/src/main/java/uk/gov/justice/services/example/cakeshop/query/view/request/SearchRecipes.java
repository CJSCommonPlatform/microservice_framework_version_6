package uk.gov.justice.services.example.cakeshop.query.view.request;

public class SearchRecipes {

    private int pagesize;
    private String name;
    private boolean glutenFree;

    public SearchRecipes(final int pagesize) {
        this.pagesize = pagesize;
    }

    public int getPagesize() {
        return pagesize;
    }

    public void setPagesize(final int pagesize) {
        this.pagesize = pagesize;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public boolean isGlutenFree() {
        return glutenFree;
    }

    public void setGlutenFree(final boolean glutenFree) {
        this.glutenFree = glutenFree;
    }
}
