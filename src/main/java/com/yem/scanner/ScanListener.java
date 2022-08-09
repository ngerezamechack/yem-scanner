/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yem.scanner;

import java.io.File;

/**
 *
 * @author NGEREZA
 */
public interface ScanListener {

    public void scanFinish(File file);

    public void cancel();
}
