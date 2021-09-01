package io.teamcode.common.vcs.svn;

/**
 * Created by chiang on 2017. 3. 24..
 */
public enum ChangedFileAction {

    ADD('+'), DELETE('-'), REPLACE(' '), MODIFY(' ');

    private char symbol;

    ChangedFileAction(char symbol) {
        this.symbol = symbol;
    }

    public char getSymbol() {
        return this.symbol;
    }
}
