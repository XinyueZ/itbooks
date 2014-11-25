package com.itbooks.utils;

import android.os.AsyncTask;
import android.os.Build;


public abstract class ParallelTask<T, Z, U> extends AsyncTask<T, Z, U> {

	public void executeParallel(T... args) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, args);
		} else {
			execute(args);
		}
	}

}
