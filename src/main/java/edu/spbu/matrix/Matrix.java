package edu.spbu.matrix;

import java.io.IOException;

/**
 *
 */
public interface Matrix {
    Matrix mul(Matrix o) throws IOException;


    Matrix dmul(Matrix o) throws IOException;

    Matrix transposition() throws IOException;

    boolean equals(Object o);

    int getRows();

    int getColumns();

    double getElement(int i, int j);

    void toSize(int cols, int rows);
}
