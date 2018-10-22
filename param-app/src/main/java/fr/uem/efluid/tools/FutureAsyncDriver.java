package fr.uem.efluid.tools;

import static fr.uem.efluid.utils.ErrorType.PREPARATION_BIZ_FAILURE;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.uem.efluid.utils.ApplicationException;

/**
 * <p>
 * Default model for async start of completable
 * </p>
 * 
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
public class FutureAsyncDriver implements AsyncDriver {

	private static final Logger LOGGER = LoggerFactory.getLogger(FutureAsyncDriver.class);

	private final ExecutorService executor;

	/**
	 * @param poolSize
	 *            for step processes
	 */
	public FutureAsyncDriver(int poolSize) {
		super();
		this.executor = Executors.newFixedThreadPool(4);
	}

	/**
	 * @param source
	 * @param action
	 * @see fr.uem.efluid.tools.AsyncDriver#start(fr.uem.efluid.tools.AsyncDriver.SourceErrorAware,
	 *      java.util.function.Consumer)
	 */
	@Override
	public <T extends SourceErrorAware> void start(T source, Consumer<T> action) {
		CompletableFuture.runAsync(() -> action.accept(source));
	}

	/**
	 * @param callables
	 * @param current
	 * @return
	 * @throws InterruptedException
	 * @see fr.uem.efluid.tools.AsyncDriver#processSteps(java.util.List,
	 *      fr.uem.efluid.tools.AsyncDriver.SourceErrorAware)
	 */
	@Override
	public <T> List<T> processSteps(List<Callable<T>> callables, final SourceErrorAware current)
			throws InterruptedException {

		return this.executor
				.invokeAll(callables)
				.stream()
				.map(c -> gatherResult(c, current))
				.sorted()
				.collect(Collectors.toList());
	}

	/**
	 * <p>
	 * Join future execution and gather exception if any
	 * </p>
	 * 
	 * @param future
	 * @return
	 */
	private static <T> T gatherResult(Future<T> future, final SourceErrorAware current) {

		try {
			return future.get();
		}

		catch (InterruptedException | ExecutionException e) {
			LOGGER.error("Error will processing diff", e);

			// If already identified, keep going on 1st identified error
			if (current.hasSourceFailure()) {
				throw current.getSourceFailure();
			}
			return current.fail(new ApplicationException(PREPARATION_BIZ_FAILURE, "Aborted on exception ", e));
		}
	}

}