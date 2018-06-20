
package com.indago.tr2d.plugins.seg;

import net.imagej.ops.OpService;
import net.imagej.ops.thread.chunker.ChunkerOp;
import net.imagej.ops.thread.chunker.CursorBasedChunk;
import org.scijava.Context;

public class Main {

	public static void main(String... args) {
		Context context = new Context();
		context.service(OpService.class).run(ChunkerOp.class,
			new CursorBasedChunk()
			{

				@Override
				public void execute(final int startIndex, final int stepSize,
					final int numSteps)
			{}
			}, 100);
	}
}
