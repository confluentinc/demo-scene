package model;

public class GitHubPRStateCounter {

    private int open;
    private int closed;

    public GitHubPRStateCounter() {
    }

    //Getters and setters needed for Jackson
    public void setOpen(int open) {
        this.open = open;
    }

    public void setClosed(int closed) {
        this.closed = closed;
    }

    public int getOpen() {
        return open;
    }

    public int getClosed() {
        return closed;
    }

    @Override
    public String toString() {
        return "GitHubPRStateCounter{" +
                "open=" + open +
                ", closed=" + closed +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GitHubPRStateCounter that)) return false;

        if (open != that.open) return false;
        return closed == that.closed;
    }

    @Override
    public int hashCode() {
        int result = open;
        result = 31 * result + closed;
        return result;
    }
}
