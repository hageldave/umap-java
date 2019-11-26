/*
 * BSD 3-Clause License
 * Copyright (c) 2017, Leland McInnes, 2019 Tag.bio (Java port).
 * See LICENSE.txt.
 */
package com.tagbio.umap;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.tagbio.umap.metric.CategoricalMetric;
import com.tagbio.umap.metric.EuclideanMetric;
import com.tagbio.umap.metric.Metric;
import com.tagbio.umap.metric.PrecomputedMetric;
import com.tagbio.umap.metric.ReducedEuclideanMetric;

// Uniform Manifold Approximation and Projection

// Finds a low dimensional embedding of the data that approximates
// an underlying manifold.

// Parameters
// ----------
// n_neighbors: float (optional, default 15)
//     The size of local neighborhood (in terms of number of neighboring
//     sample points) used for manifold approximation. Larger values
//     result in more global views of the manifold, while smaller
//     values result in more local data being preserved. In general
//     values should be in the range 2 to 100.

// n_components: int (optional, default 2)
//     The dimension of the space to embed into. This defaults to 2 to
//     provide easy visualization, but can reasonably be set to any
//     integer value in the range 2 to 100.

// metric: string || function (optional, default 'euclidean')
//     The metric to use to compute distances in high dimensional space.
//     If a string is passed it must match a valid predefined metric. If
//     a general metric is required a function that takes two 1d arrays and
//     returns a float can be provided. For performance purposes it is
//     required that this be a numba jit'd function. Valid string metrics
//     include:
//         * euclidean
//         * manhattan
//         * chebyshev
//         * minkowski
//         * canberra
//         * braycurtis
//         * mahalanobis
//         * wminkowski
//         * seuclidean
//         * cosine
//         * correlation
//         * haversine
//         * hamming
//         * jaccard
//         * dice
//         * russelrao
//         * kulsinski
//         * rogerstanimoto
//         * sokalmichener
//         * sokalsneath
//         * yule
//     Metrics that take arguments (such as minkowski, mahalanobis etc.)
//     can have arguments passed via the metric_kwds dictionary. At this
//     time care must be taken and dictionary elements must be ordered
//     appropriately; this will hopefully be fixed in the future.

// n_epochs: int (optional, default null)
//     The number of training epochs to be used in optimizing the
//     low dimensional embedding. Larger values result in more accurate
//     embeddings. If null is specified a value will be selected based on
//     the size of the input dataset (200 for large datasets, 500 for small).

// learning_rate: float (optional, default 1.0)
//     The initial learning rate for the embedding optimization.

// init: string (optional, default 'spectral')
//     How to initialize the low dimensional embedding. Options are:
//         * 'spectral': use a spectral embedding of the fuzzy 1-skeleton
//         * 'random': assign initial embedding positions at random.
//         * A numpy array of initial embedding positions.

// min_dist: float (optional, default 0.1)
//     The effective minimum distance between embedded points. Smaller values
//     will result in a more clustered/clumped embedding where nearby points
//     on the manifold are drawn closer together, while larger values will
//     result on a more even dispersal of points. The value should be set
//     relative to the ``spread`` value, which determines the scale at which
//     embedded points will be spread out.

// spread: float (optional, default 1.0)
//     The effective scale of embedded points. In combination with ``min_dist``
//     this determines how clustered/clumped the embedded points are.

// set_op_mix_ratio: float (optional, default 1.0)
//     Interpolate between (fuzzy) union and intersection as the set operation
//     used to combine local fuzzy simplicial sets to obtain a global fuzzy
//     simplicial sets. Both fuzzy set operations use the product t-norm.
//     The value of this parameter should be between 0.0 and 1.0; a value of
//     1.0 will use a pure fuzzy union, while 0.0 will use a pure fuzzy
//     intersection.

// local_connectivity: int (optional, default 1)
//     The local connectivity required -- i.e. the number of nearest
//     neighbors that should be assumed to be connected at a local level.
//     The higher this value the more connected the manifold becomes
//     locally. In practice this should be not more than the local intrinsic
//     dimension of the manifold.

// repulsion_strength: float (optional, default 1.0)
//     Weighting applied to negative samples in low dimensional embedding
//     optimization. Values higher than one will result in greater weight
//     being given to negative samples.

// negative_sample_rate: int (optional, default 5)
//     The number of negative samples to select per positive sample
//     in the optimization process. Increasing this value will result
//     in greater repulsive force being applied, greater optimization
//     cost, but slightly more accuracy.

// transform_queue_size: float (optional, default 4.0)
//     For transform operations (embedding new points using a trained model_
//     this will control how aggressively to search for nearest neighbors.
//     Larger values will result in slower performance but more accurate
//     nearest neighbor evaluation.

// a: float (optional, default null)
//     More specific parameters controlling the embedding. If null these
//     values are set automatically as determined by ``min_dist`` and
//     ``spread``.
// b: float (optional, default null)
//     More specific parameters controlling the embedding. If null these
//     values are set automatically as determined by ``min_dist`` and
//     ``spread``.

// random: int, RandomState instance || null, optional (default: null)
//     If int, random is the seed used by the random number generator;
//     If RandomState instance, random is the random number generator;
//     If null, the random number generator is the RandomState instance used
//     by `np.random`.

// metric_kwds: dict (optional, default null)
//     Arguments to pass on to the metric, such as the ``p`` value for
//     Minkowski distance. If null then no arguments are passed on.

// angular_rp_forest: bool (optional, default false)
//     Whether to use an angular random projection forest to initialise
//     the approximate nearest neighbor search. This can be faster, but is
//     mostly on useful for metric that use an angular style distance such
//     as cosine, correlation etc. In the case of those metrics angular forests
//     will be chosen automatically.

// target_n_neighbors: int (optional, default -1)
//     The number of nearest neighbors to use to construct the target simplcial
//     set. If set to -1 use the ``n_neighbors`` value.

// target_metric: string || callable (optional, default 'categorical')
//     The metric used to measure distance for a target array is using supervised
//     dimension reduction. By default this is 'categorical' which will measure
//     distance in terms of whether categories match || are different. Furthermore,
//     if semi-supervised is required target values of -1 will be trated as
//     unlabelled under the 'categorical' metric. If the target array takes
//     continuous values (e.g. for a regression problem) then metric of 'l1'
//     || 'l2' is probably more appropriate.

// target_metric_kwds: dict (optional, default null)
//     Keyword argument to pass to the target metric when performing
//     supervised dimension reduction. If null then no arguments are passed on.

// target_weight: float (optional, default 0.5)
//     weighting factor between data topology and target topology. A value of
//     0.0 weights entirely on data, a value of 1.0 weights entirely on target.
//     The default of 0.5 balances the weighting equally between data and target.

// transform_seed: int (optional, default 42)
//     Random seed used for the stochastic aspects of the transform operation.
//     This ensures consistency in transform operations.

// verbose: bool (optional, default false)
//     Controls verbosity of logging.
public class Umap {

//  private static final int INT32_MIN = Integer.MIN_VALUE + 1;
//  private static final int INT32_MAX = Integer.MAX_VALUE - 1;

  private static final double SMOOTH_K_TOLERANCE = 1e-5;
  private static final double MIN_K_DIST_SCALE = 1e-3;
  private static final double NPY_INFINITY = Double.POSITIVE_INFINITY;

  private static final int SMALL_PROBLEM_THRESHOLD = 100; //4096;

  /*
    Compute a continuous version of the distance to the kth nearest
    neighbor. That is, this is similar to knn-distance but allows continuous
    k values rather than requiring an integral k. In essence we are simply
    computing the distance such that the cardinality of fuzzy set we generate
    is k.

    Parameters
    ----------
    distances: array of shape (n_samples, n_neighbors)
        Distances to nearest neighbors for each samples. Each row should be a
        sorted list of distances to a given samples nearest neighbors.

    k: float
        The number of nearest neighbors to approximate for.

    nIter: int (optional, default 64)
        We need to binary search for the correct distance value. This is the
        max number of iterations to use in such a search.

    localConnectivity: int (optional, default 1)
        The local connectivity required -- i.e. the number of nearest
        neighbors that should be assumed to be connected at a local level.
        The higher this value the more connected the manifold becomes
        locally. In practice this should be not more than the local intrinsic
        dimension of the manifold.

    bandwidth: float (optional, default 1)
        The target bandwidth of the kernel, larger values will produce
        larger return values.

    Returns
    -------
    knn_dist: array of shape (n_samples,)
        The distance to kth nearest neighbor, as suitably approximated.

    nn_dist: array of shape (n_samples,)
        The distance to the 1st nearest neighbor for each point.
  */
  private static float[][] smoothKnnDist(float[][] distances, double k, int nIter, double localConnectivity, double bandwidth) {
    final double target = MathUtils.log2(k) * bandwidth;
    final float[] rho = new float[distances.length];
    final float[] result = new float[distances.length];

    final double meanDistances = MathUtils.mean(distances);

    for (int i = 0; i < distances.length; ++i) {
      double lo = 0.0;
      double hi = NPY_INFINITY;
      double mid = 1.0;

      final float[] ithDistances = distances[i];
      final float[] nonZeroDists = MathUtils.filterPositive(ithDistances);
      if (nonZeroDists.length >= localConnectivity) {
        final int index = (int) Math.floor(localConnectivity);
        final double interpolation = localConnectivity - index;
        if (index > 0) {
          rho[i] = nonZeroDists[index - 1];
          if (interpolation > SMOOTH_K_TOLERANCE) {
            rho[i] += interpolation * (nonZeroDists[index] - nonZeroDists[index - 1]);
          }
        } else {
          rho[i] = (float) (interpolation * nonZeroDists[0]);
        }
      } else if (nonZeroDists.length > 0) {
        rho[i] = MathUtils.max(nonZeroDists);
      }

      for (int n = 0; n < nIter; ++n) {
        double pSum = 0.0;
        for (int j = 1; j < distances[0].length; ++j) {
          final double d = distances[i][j] - rho[i];
          pSum += d > 0 ? Math.exp(-(d / mid)) : 1;
        }

        if (Math.abs(pSum - target) < SMOOTH_K_TOLERANCE) {
          break;
        }

        if (pSum > target) {
          hi = mid;
          mid = (lo + hi) / 2.0;
        } else {
          lo = mid;
          if (hi == NPY_INFINITY) {
            mid *= 2;
          } else {
            mid = (lo + hi) / 2.0;
          }
        }
      }

      result[i] = (float) mid;

      if (rho[i] > 0.0) {
        double meanIthDistances = MathUtils.mean(ithDistances);
        if (result[i] < MIN_K_DIST_SCALE * meanIthDistances) {
          result[i] = (float) (MIN_K_DIST_SCALE * meanIthDistances);
        }
      } else {
        if (result[i] < MIN_K_DIST_SCALE * meanDistances) {
          result[i] = (float) (MIN_K_DIST_SCALE * meanDistances);
        }
      }
    }
    return new float[][]{result, rho};
  }

  private static float[][] smoothKnnDist(float[][] distances, double k, double localConnectivitiy) {
    return smoothKnnDist(distances, k, 64, localConnectivitiy, 1.0);
  }

  // Compute the ``n_neighbors`` nearest points for each data point in ``X``
  // under ``metric``. This may be exact, but more likely is approximated via
  // nearest neighbor descent.

  // Parameters
  // ----------
  // X: array of shape (n_samples, n_features)
  //     The input data to compute the k-neighbor graph of.

  // n_neighbors: int
  //     The number of nearest neighbors to compute for each sample in ``X``.

  // metric: string || callable
  //     The metric to use for the computation.

  // metric_kwds: dict
  //     Any arguments to pass to the metric computation function.

  // angular: bool
  //     Whether to use angular rp trees in NN approximation.

  // random: np.random state
  //     The random state to use for approximate NN computations.

  // verbose: bool
  //     Whether to print status data during the computation.

  // Returns
  // -------
  // knn_indices: array of shape (n_samples, n_neighbors)
  //     The indices on the ``n_neighbors`` closest points in the dataset.

  // knn_dists: array of shape (n_samples, nNeighbors)
  //     The distances to the ``nNeighbors`` closest points in the dataset.
  private static Object[] nearestNeighbors(final Matrix instances, final int nNeighbors, Metric metric, boolean angular, final Random random, final boolean verbose) {
    if (verbose) {
      Utils.message("Finding Nearest Neighbors");
    }

    int[][] knnIndices = null;
    float[][] knnDists = null;
    List<FlatTree> rpForest = null;
    if (metric.equals(PrecomputedMetric.SINGLETON)) {
      // Note that this does not support sparse distance matrices yet ...
      // Compute indices of n nearest neighbors
      knnIndices = Utils.fastKnnIndices(instances, nNeighbors);
      // Compute the nearest neighbor distances
      //   (equivalent to np.sort(X)[:,:nNeighbors])
      //knnDists = X[np.arange(X.rows())[:, None],knnIndices].copy();
      knnDists = new float[knnIndices.length][nNeighbors];
      for (int i = 0; i < knnDists.length; ++i) {
        for (int j = 0; j < nNeighbors; ++j) {
          knnDists[i][j] = instances.get(i, knnIndices[i][j]);
        }
      }
      rpForest = Collections.emptyList();
    } else {
      Metric distance_func = metric;
      angular = distance_func.isAngular();

      if (instances instanceof CsrMatrix) {
        final CsrMatrix csrInstances = (CsrMatrix) instances;
        // todo this is nonsense now, since metric cannot be a string at this point
        if (Sparse.sparse_named_distances.containsKey(metric)) {
          distance_func = Sparse.sparse_named_distances.get(metric);
//          if (Sparse.sparse_need_n_features.contains(metric)) {
//            metricKwds.put("n_features", instances.cols());
//          }
        } else {
          throw new IllegalArgumentException("Metric " + metric + " not supported for sparse data");
        }
        throw new UnsupportedOperationException();
//        metric_nn_descent = Sparse.make_sparse_nn_descent(distance_func, tuple(metricKwds.values()));
//
//        int n_trees = 5 + (int) (Math.round(Math.pow(Y.rows(), 0.5 / 20.0)));
//        int n_iters = Math.max(5, (int) (Math.round(MathUtils.log2(Y.rows()))));
//        if (verbose) {
//          Utils.message("Building RP forest with " + n_trees + " trees");
//        }
//
//        rpForest = RpTree.make_forest(X, nNeighbors, n_trees, random, angular);
//        final int[][] leaf_array = RpTree.rptree_leaf_array(rpForest);
//
//        if (verbose) {
//          Utils.message("NN descent for " + n_iters + " iterations");
//        }
//        final Object[] nn = NearestNeighborDescent.metric_nn_descent(Y.indices, Y.indptr, Y.data, Y.rows(), nNeighbors, random, /*max_candidates=*/60, /*rp_tree_init=*/true,     /*leaf_array=*/leaf_array,  /*n_iters=*/n_iters, verbose);
//        knnIndices = (int[][]) nn[0];
//        knnDists = (float[][]) nn[1];
      } else {
        final NearestNeighborDescent metric_nn_descent = new NearestNeighborDescent(distance_func);
        int nTrees = 5 + (int) (Math.round(Math.pow(instances.rows(), 0.5 / 20.0)));
        int nIters = Math.max(5, (int) (Math.round(MathUtils.log2(instances.rows()))));

        if (verbose) {
          Utils.message("Building RP forest with " + nTrees + " trees");
        }
        rpForest = RandomProjectionTree.makeForest(instances, nNeighbors, nTrees, random, angular);
        final int[][] leaf_array = RandomProjectionTree.rptree_leaf_array(rpForest);
        if (verbose) {
          Utils.message("NN descent for " + nIters + " iterations");
        }
        final Heap nn = metric_nn_descent.nn_descent(instances, nNeighbors, random, 60, true, nIters, leaf_array, verbose);
        knnIndices = nn.indices;
        knnDists = nn.weights;
      }

      if (MathUtils.containsNegative(knnIndices)) {
        Utils.message("Failed to correctly find nNeighbors for some samples. Results may be less than ideal. Try re-running with different parameters.");
      }
    }
    if (verbose) {
      Utils.message("Finished Nearest Neighbor Search");
    }
    return new Object[]{knnIndices, knnDists, rpForest};
  }


  // Construct the membership strength data for the 1-skeleton of each local
  // fuzzy simplicial set -- this is formed as a sparse matrix where each row is
  // a local fuzzy simplicial set, with a membership strength for the
  // 1-simplex to each other data point.

  // Parameters
  // ----------
  // knn_indices: array of shape (n_samples, n_neighbors)
  //     The indices on the ``n_neighbors`` closest points in the dataset.

  // knn_dists: array of shape (n_samples, n_neighbors)
  //     The distances to the ``n_neighbors`` closest points in the dataset.

  // sigmas: array of shape(n_samples)
  //     The normalization factor derived from the metric tensor approximation.

  // rhos: array of shape(n_samples)
  //     The local connectivity adjustment.

  // Returns
  // -------
  // rows: array of shape (n_samples * n_neighbors)
  //     Row data for the resulting sparse matrix (coo format)

  // cols: array of shape (n_samples * n_neighbors)
  //     Column data for the resulting sparse matrix (coo format)

  // vals: array of shape (n_samples * n_neighbors)
  //     Entries for the resulting sparse matrix (coo format)
  private static CooMatrix computeMembershipStrengths(final int[][] knnIndices, final float[][] knnDists, final float[] sigmas, final float[] rhos, final int[] shape) {
    final int nSamples = knnIndices.length;
    final int nNeighbors = knnIndices[0].length;
    final int size = nSamples * nNeighbors;

    final int[] rows = new int[size];
    final int[] cols = new int[size];
    final float[] vals = new float[size];

    for (int i = 0; i < nSamples; ++i) {
      for (int j = 0; j < nNeighbors; ++j) {
        if (knnIndices[i][j] == -1) {
          continue;  // We didn't get the full knn for i
        }
        final double val;
        if (knnIndices[i][j] == i) {
          val = 0.0;
        } else if (knnDists[i][j] - rhos[i] <= 0.0) {
          val = 1.0;
        } else {
          val = Math.exp(-((knnDists[i][j] - rhos[i]) / (sigmas[i])));
        }
        rows[i * nNeighbors + j] = i;
        cols[i * nNeighbors + j] = knnIndices[i][j];
        vals[i * nNeighbors + j] = (float) val;
      }
    }
    return new CooMatrix(vals, rows, cols, shape);
  }


  // Given a set of data X, a neighborhood size, and a measure of distance
  // compute the fuzzy simplicial set (here represented as a fuzzy graph in
  // the form of a sparse matrix) associated to the data. This is done by
  // locally approximating geodesic distance at each point, creating a fuzzy
  // simplicial set for each such point, and then combining all the local
  // fuzzy simplicial sets into a global one via a fuzzy union.

  // Parameters
  // ----------
  // X: array of shape (n_samples, n_features)
  //     The data to be modelled as a fuzzy simplicial set.

  // n_neighbors: int
  //     The number of neighbors to use to approximate geodesic distance.
  //     Larger numbers induce more global estimates of the manifold that can
  //     miss finer detail, while smaller values will focus on fine manifold
  //     structure to the detriment of the larger picture.

  // random: numpy RandomState || equivalent
  //     A state capable being used as a numpy random state.

  // metric: string || function (optional, default 'euclidean')
  //     The metric to use to compute distances in high dimensional space.
  //     If a string is passed it must match a valid predefined metric. If
  //     a general metric is required a function that takes two 1d arrays and
  //     returns a float can be provided. For performance purposes it is
  //     required that this be a numba jit'd function. Valid string metrics
  //     include:
  //         * euclidean (or l2)
  //         * manhattan (or l1)
  //         * cityblock
  //         * braycurtis
  //         * canberra
  //         * chebyshev
  //         * correlation
  //         * cosine
  //         * dice
  //         * hamming
  //         * jaccard
  //         * kulsinski
  //         * mahalanobis
  //         * matching
  //         * minkowski
  //         * rogerstanimoto
  //         * russellrao
  //         * seuclidean
  //         * sokalmichener
  //         * sokalsneath
  //         * sqeuclidean
  //         * yule
  //         * wminkowski

  //     Metrics that take arguments (such as minkowski, mahalanobis etc.)
  //     can have arguments passed via the metric_kwds dictionary. At this
  //     time care must be taken and dictionary elements must be ordered
  //     appropriately; this will hopefully be fixed in the future.

  // knn_indices: array of shape (n_samples, n_neighbors) (optional)
  //     If the k-nearest neighbors of each point has already been calculated
  //     you can pass them in here to save computation time. This should be
  //     an array with the indices of the k-nearest neighbors as a row for
  //     each data point.

  // knn_dists: array of shape (n_samples, n_neighbors) (optional)
  //     If the k-nearest neighbors of each point has already been calculated
  //     you can pass them in here to save computation time. This should be
  //     an array with the distances of the k-nearest neighbors as a row for
  //     each data point.

  // angular: bool (optional, default false)
  //     Whether to use angular/cosine distance for the random projection
  //     forest for seeding NN-descent to determine approximate nearest
  //     neighbors.

  // set_op_mix_ratio: float (optional, default 1.0)
  //     Interpolate between (fuzzy) union and intersection as the set operation
  //     used to combine local fuzzy simplicial sets to obtain a global fuzzy
  //     simplicial sets. Both fuzzy set operations use the product t-norm.
  //     The value of this parameter should be between 0.0 and 1.0; a value of
  //     1.0 will use a pure fuzzy union, while 0.0 will use a pure fuzzy
  //     intersection.

  // local_connectivity: int (optional, default 1)
  //     The local connectivity required -- i.e. the number of nearest
  //     neighbors that should be assumed to be connected at a local level.
  //     The higher this value the more connected the manifold becomes
  //     locally. In practice this should be not more than the local intrinsic
  //     dimension of the manifold.

  // verbose: bool (optional, default false)
  //     Whether to report information on the current progress of the algorithm.

  // Returns
  // -------
  // fuzzy_simplicial_set: coo_matrix
  //     A fuzzy simplicial set represented as a sparse matrix. The (i,
  //     j) entry of the matrix represents the membership strength of the
  //     1-simplex between the ith and jth sample points.
  private static Matrix fuzzySimplicialSet(final Matrix instances, final int nNeighbors, final Random random, final Metric metric, int[][] knnIndices, float[][] knnDists, final boolean angular, final float setOpMixRatio, final float localConnectivity, final boolean verbose) {

    if (knnIndices == null || knnDists == null) {
      final Object[] nn = nearestNeighbors(instances, nNeighbors, metric, angular, random, verbose);
      knnIndices = (int[][]) nn[0];
      knnDists = (float[][]) nn[1];
    }

    final float[][] sigmasRhos = smoothKnnDist(knnDists, nNeighbors, localConnectivity);
    final float[] sigmas = sigmasRhos[0];
    final float[] rhos = sigmasRhos[1];

    final Matrix result = computeMembershipStrengths(knnIndices, knnDists, sigmas, rhos, new int[]{instances.rows(), instances.rows()}).eliminateZeros();
    final Matrix prodMatrix = result.hadamardMultiplyTranspose();

    return result.addTranspose().subtract(prodMatrix).multiply(setOpMixRatio).add(prodMatrix.multiply(1.0F - setOpMixRatio)).eliminateZeros();
  }


  // Under the assumption of categorical distance for the intersecting
  // simplicial set perform a fast intersection.

  // Parameters
  // ----------
  // rows: array
  //     An array of the row of each non-zero in the sparse matrix
  //     representation.

  // cols: array
  //     An array of the column of each non-zero in the sparse matrix
  //     representation.

  // values: array
  //     An array of the value of each non-zero in the sparse matrix
  //     representation.

  // target: array of shape (n_samples)
  //     The categorical labels to use in the intersection.

  // unknown_dist: float (optional, default 1.0)
  //     The distance an unknown label (-1) is assumed to be from any point.

  // far_dist float (optional, default 5.0)
  //     The distance between unmatched labels.

  // Returns
  // -------
  // null
  private static void fastIntersection(final int[] rows, final int[] cols, final float[] values, final float[] target, final double unknownDist, final double farDist) {
    for (int nz = 0; nz < rows.length; ++nz) {
      final int i = rows[nz];
      final int j = cols[nz];
      if (target[i] == -1 || target[j] == -1) {
        values[nz] *= Math.exp(-unknownDist);
      } else if (target[i] != target[j]) {
        values[nz] *= Math.exp(-farDist);
      }
    }
  }

  // Reset the local connectivity requirement -- each data sample should
  // have complete confidence in at least one 1-simplex in the simplicial set.
  // We can enforce this by locally rescaling confidences, and then remerging the
  // different local simplicial sets together.

  // Parameters
  // ----------
  // simplicial_set: sparse matrix
  //     The simplicial set for which to recalculate with respect to local
  //     connectivity.

  // Returns
  // -------
  // simplicialSet: sparse_matrix
  //     The recalculated simplicial set, now with the local connectivity
  //     assumption restored.
  private static Matrix resetLocalConnectivity(final Matrix simplicialSet) {
    final Matrix nss = Normalize.normalize(simplicialSet, "max");
    final Matrix prodMatrix = nss.hadamardMultiplyTranspose();
    return nss.addTranspose().subtract(prodMatrix).eliminateZeros();
  }

  // Combine a fuzzy simplicial set with another fuzzy simplicial set
  // generated from categorical data using categorical distances. The target
  // data is assumed to be categorical label data (a vector of labels),
  // and this will update the fuzzy simplicial set to respect that label data.

  // Parameters
  // ----------
  // simplicialSet: sparse matrix
  //     The input fuzzy simplicial set.

  // target: array of shape (n_samples)
  //     The categorical labels to use in the intersection.

  // unknownDist: float (optional, default 1.0)
  //     The distance an unknown label (-1) is assumed to be from any point.

  // farDist float (optional, default 5.0)
  //     The distance between unmatched labels.

  // Returns
  // -------
  // simplicialSet: sparse matrix
  //     The resulting intersected fuzzy simplicial set.
  private static Matrix categoricalSimplicialSetIntersection(final CooMatrix simplicialSet, final float[] target, final float unknownDist, final float farDist) {
    fastIntersection(simplicialSet.mRow, simplicialSet.mCol, simplicialSet.mData, target, unknownDist, farDist);
    return resetLocalConnectivity(simplicialSet.eliminateZeros());
  }

  private static Matrix categoricalSimplicialSetIntersection(final CooMatrix simplicialSet, final float[] target, final float farDist) {
    return categoricalSimplicialSetIntersection(simplicialSet, target, 1.0F, farDist);
  }

  private static Matrix generalSimplicialSetIntersection(final Matrix simplicialSet1, final Matrix simplicialSet2, final float weight) {

    final CooMatrix result = simplicialSet1.add(simplicialSet2).toCoo();
    final CsrMatrix left = simplicialSet1.toCsr();
    final CsrMatrix right = simplicialSet2.toCsr();

    Sparse.generalSsetIntersection(left.indptr, left.indices, left.data, right.indptr, right.indices, right.data, result.mRow, result.mCol, result.mData, weight);

    return result;
  }

  // Given a set of weights and number of epochs generate the number of
  // epochs per sample for each weight.

  // Parameters
  // ----------
  // weights: array of shape (n_1_simplices)
  //     The weights of how much we wish to sample each 1-simplex.

  // n_epochs: int
  //     The total number of epochs we want to train for.

  // Returns
  // -------
  // An array of number of epochs per sample, one for each 1-simplex.
  private static float[] makeEpochsPerSample(final float[] weights, final int nEpochs) {
    final float[] result = new float[weights.length];
    Arrays.fill(result, -1.0F);
    final float[] nSamples = MathUtils.multiply(MathUtils.divide(weights, MathUtils.max(weights)), nEpochs);
    for (int k = 0; k < nSamples.length; ++k) {
      if (nSamples[k] > 0) {
        result[k] = nEpochs / nSamples[k];
      }
    }
    return result;
  }


  // Standard clamping of a value into a fixed range (in this case -4.0 to
  // 4.0)

  // Parameters
  // ----------
  // val: float
  //     The value to be clamped.

  // Returns
  // -------
  // The clamped value, now fixed to be in the range -4.0 to 4.0.
  private static double clip(final double val) {
    if (val > 4.0) {
      return 4.0;
    } else if (val < -4.0) {
      return -4.0;
    } else {
      return val;
    }
  }


  // Reduced Euclidean distance.
  // Parameters
  // ----------
  // x: array of shape (embedding_dim,)
  // y: array of shape (embedding_dim,)

  // Returns
  // -------
  // The squared euclidean distance between x and y
  private static double rdist(float[] x, float[] y) {
    return ReducedEuclideanMetric.SINGLETON.distance(x, y);
  }


  // Improve an embedding using stochastic gradient descent to minimize the
  // fuzzy set cross entropy between the 1-skeletons of the high dimensional
  // and low dimensional fuzzy simplicial sets. In practice this is done by
  // sampling edges based on their membership strength (with the (1-p) terms
  // coming from negative sampling similar to word2vec).

  // Parameters
  // ----------
  // head_embedding: array of shape (n_samples, n_components)
  //     The initial embedding to be improved by SGD.

  // tail_embedding: array of shape (source_samples, n_components)
  //     The reference embedding of embedded points. If not embedding new
  //     previously unseen points with respect to an existing embedding this
  //     is simply the head_embedding (again); otherwise it provides the
  //     existing embedding to embed with respect to.

  // head: array of shape (n_1_simplices)
  //     The indices of the heads of 1-simplices with non-zero membership.

  // tail: array of shape (n_1_simplices)
  //     The indices of the tails of 1-simplices with non-zero membership.

  // n_epochs: int
  //     The number of training epochs to use in optimization.

  // n_vertices: int
  //     The number of vertices (0-simplices) in the dataset.

  // epochs_per_samples: array of shape (n_1_simplices)
  //     A float value of the number of epochs per 1-simplex. 1-simplices with
  //     weaker membership strength will have more epochs between being sampled.

  // a: float
  //     Parameter of differentiable approximation of right adjoint functor

  // b: float
  //     Parameter of differentiable approximation of right adjoint functor

  // random: array of int64, shape (3,)
  //     The internal state of the rng

  // gamma: float (optional, default 1.0)
  //     Weight to apply to negative samples.

  // initial_alpha: float (optional, default 1.0)
  //     Initial learning rate for the SGD.

  // negative_sample_rate: int (optional, default 5)
  //     Number of negative samples to use per positive sample.

  // verbose: bool (optional, default false)
  //     Whether to report information on the current progress of the algorithm.

  // Returns
  // -------
  // embedding: array of shape (n_samples, n_components)
  //     The optimized embedding.
  private static Matrix optimizeLayout(final Matrix headEmbedding, final Matrix tailEmbedding, final int[] head, final int[] tail, final int nEpochs, final int nVertices, final float[] epochsPerSample, final float a, final float b, final Random random, final float gamma, final float initialAlpha, final float negativeSampleRate, final boolean verbose) {

    assert headEmbedding instanceof DefaultMatrix;

    final int dim = headEmbedding.cols();
    final boolean moveOther = (headEmbedding.rows() == tailEmbedding.rows());
    double alpha = initialAlpha;

    final float[] epochsPerNegativeSample = MathUtils.divide(epochsPerSample, negativeSampleRate);
    final float[] epochOfNextNegativeSample = Arrays.copyOf(epochsPerNegativeSample, epochsPerNegativeSample.length);
    final float[] epochOfNextSample = Arrays.copyOf(epochsPerSample, epochsPerSample.length);

    for (int n = 0; n < nEpochs; ++n) {
      for (int i = 0; i < epochsPerSample.length; ++i) {
        if (epochOfNextSample[i] <= n) {
          final int j = head[i];
          int k = tail[i];
          // todo this assumes that current is a pointer to the internal matrix data
          final float[] current = headEmbedding.row(j);
          float[] other = tailEmbedding.row(k);

          double distSquared = rdist(current, other);

          double gradCoeff;
          if (distSquared > 0.0) {
            gradCoeff = (-2.0 * a * b * Math.pow(distSquared, b - 1.0));
            gradCoeff /= (a * Math.pow(distSquared, b) + 1.0);
          } else {
            gradCoeff = 0.0;
          }

          for (int d = 0; d < dim; ++d) {
            final double grad_d = clip(gradCoeff * (current[d] - other[d]));
            current[d] += grad_d * alpha;
            if (moveOther) {
              other[d] += -grad_d * alpha;
            }
          }

          epochOfNextSample[i] += epochsPerSample[i];

          final int nNegSamples = (int) ((n - epochOfNextNegativeSample[i]) / epochsPerNegativeSample[i]);

          for (int p = 0; p < nNegSamples; ++p) {
            k = random.nextInt(nVertices);

            other = tailEmbedding.row(k);

            distSquared = rdist(current, other);

            if (distSquared > 0.0) {
              gradCoeff = 2.0 * gamma * b;
              gradCoeff /= (0.001 + distSquared) * (a * Math.pow(distSquared, b) + 1);
            } else if (j == k) {
              continue;
            } else {
              gradCoeff = 0.0;
            }

            for (int d = 0; d < dim; ++d) {
              final double grad_d;
              if (gradCoeff > 0.0) {
                grad_d = clip(gradCoeff * (current[d] - other[d]));
              } else {
                grad_d = 4.0;
              }
              current[d] += grad_d * alpha;
            }
          }

          epochOfNextNegativeSample[i] += nNegSamples * epochsPerNegativeSample[i];
        }
      }

      alpha = initialAlpha * (1.0 - (float) n / (float) (nEpochs));

      if (verbose && n % (nEpochs / 10) == 0) {
        Utils.message("\tcompleted " + n + "/" + nEpochs + " epochs");
      }
    }
    return headEmbedding;
  }


  // Perform a fuzzy simplicial set embedding, using a specified
  // initialisation method and then minimizing the fuzzy set cross entropy
  // between the 1-skeletons of the high and low dimensional fuzzy simplicial
  // sets.

  // Parameters
  // ----------
  // data: array of shape (n_samples, n_features)
  //     The source data to be embedded by UMAP.

  // graph: sparse matrix
  //     The 1-skeleton of the high dimensional fuzzy simplicial set as
  //     represented by a graph for which we require a sparse matrix for the
  //     (weighted) adjacency matrix.

  // n_components: int
  //     The dimensionality of the euclidean space into which to embed the data.

  // initial_alpha: float
  //     Initial learning rate for the SGD.

  // a: float
  //     Parameter of differentiable approximation of right adjoint functor

  // b: float
  //     Parameter of differentiable approximation of right adjoint functor

  // gamma: float
  //     Weight to apply to negative samples.

  // negative_sample_rate: int (optional, default 5)
  //     The number of negative samples to select per positive sample
  //     in the optimization process. Increasing this value will result
  //     in greater repulsive force being applied, greater optimization
  //     cost, but slightly more accuracy.

  // n_epochs: int (optional, default 0)
  //     The number of training epochs to be used in optimizing the
  //     low dimensional embedding. Larger values result in more accurate
  //     embeddings. If 0 is specified a value will be selected based on
  //     the size of the input dataset (200 for large datasets, 500 for small).

  // init: string
  //     How to initialize the low dimensional embedding. Options are:
  //         * 'spectral': use a spectral embedding of the fuzzy 1-skeleton
  //         * 'random': assign initial embedding positions at random.
  //         * A numpy array of initial embedding positions.

  // random: numpy RandomState || equivalent
  //     A state capable being used as a numpy random state.

  // metric: string
  //     The metric used to measure distance in high dimensional space; used if
  //     multiple connected components need to be layed out.

  // metric_kwds: dict
  //     Key word arguments to be passed to the metric function; used if
  //     multiple connected components need to be layed out.

  // verbose: bool (optional, default false)
  //     Whether to report information on the current progress of the algorithm.

  // Returns
  // -------
  // embedding: array of shape (n_samples, nComponents)
  //     The optimized of ``graph`` into an ``nComponents`` dimensional
  //     euclidean space.
  private static Matrix simplicialSetEmbedding(Matrix data, Matrix graph_in, int nComponents, float initialAlpha, float a, float b, float gamma, int negativeSampleRate, int nEpochs, String init, Random random, Metric metric, boolean verbose) {

    CooMatrix graph = graph_in.toCoo();
    //graph.sum_duplicates();
    int nVertices = graph.cols();

    if (nEpochs <= 0) {
      // For smaller datasets we can use more epochs
      if (graph.rows() <= 10000) {
        nEpochs = 500;
      } else {
        nEpochs = 200;
      }
    }

    MathUtils.zeroEntriesBelowLimit(graph.mData, MathUtils.max(graph.mData) / (float) nEpochs);
    graph = (CooMatrix) graph.eliminateZeros();

    Matrix embedding;
    if ("random".equals(init)) {
      //embedding = random.uniform(low = -10.0, high = 10.0, size = (graph.rows(), nComponents)).astype(np.float32);
      embedding = new DefaultMatrix(MathUtils.uniform(random, -10, 10, graph.rows(), nComponents));
    } else if ("spectral".equals(init)) {
      throw new UnsupportedOperationException();
//      // We add a little noise to avoid local minima for optimization to come
//      float[][] initialisation = Spectral.spectral_layout(data, graph, nComponents, random, /*metric=*/metric, /*metric_kwds=*/metric_kwds);
//      float expansion = 10.0 / Math.abs(initialisation).max();
//      embedding = (MathUtils.multiply(initialisation, expansion)).astype(np.float32) + random.normal(scale = 0.0001, size =[graph.rows(), nComponents]).astype(np.float32);
    } else {
      // Situation where init contains prepared data
      throw new UnsupportedOperationException();
//      init_data = np.array(init);
//      if (len(init_data.shape) == 2) {
//        if (np.unique(init_data, /*axis =*/ 0).length < init_data.length) {
//          tree = KDTree(init_data);
//          float[][] dist /*, ind*/ = tree.query(init_data, k = 2);
//          double nndist = MathUtils.mean(dist, 1);
//          embedding = init_data + random.normal(scale = 0.001 * nndist, size = init_data.shape).astype(np.float32);
//        } else {
//          embedding = init_data;
//        }
//      }
    }

    final float[] epochsPerSample = makeEpochsPerSample(graph.mData, nEpochs);
    final int[] head = graph.mRow;
    final int[] tail = graph.mCol;

    // so (head, tail, epochsPerSample) is like a CooMatrix

    return optimizeLayout(embedding, embedding, head, tail, nEpochs, nVertices, epochsPerSample, a, b, random, gamma, initialAlpha, negativeSampleRate, verbose);
  }


  // Given indices and weights and an original embeddings
  // initialize the positions of new points relative to the
  // indices and weights (of their neighbors in the source data).

  // Parameters
  // ----------
  // indices: array of shape (n_new_samples, n_neighbors)
  //     The indices of the neighbors of each new sample

  // weights: array of shape (n_new_samples, n_neighbors)
  //     The membership strengths of associated 1-simplices
  //     for each of the new samples.

  // embedding: array of shape (n_samples, dim)
  //     The original embedding of the source data.

  // Returns
  // -------
  // new_embedding: array of shape (n_new_samples, dim)
  //     An initial embedding of the new sample points.
  private static Matrix init_transform(final int[][] indices, final float[][] weights, final Matrix embedding) {
    final float[][] result = new float[indices.length][embedding.cols()];
    for (int i = 0; i < indices.length; ++i) {
      for (int j = 0; j < indices[i].length; ++j) {
        for (int d = 0; d < embedding.cols(); ++d) {
          result[i][d] += (weights[i][j] * embedding.get(indices[i][j], d));
        }
      }
    }

    return new DefaultMatrix(result);
  }

  private static double curve(final float x, final float a, final float b) {
    return 1.0 / (1.0 + a * Math.pow(x, 2 * b));
  }

  // Fit a, b params for the differentiable curve used in lower
  // dimensional fuzzy simplicial complex construction. We want the
  // smooth curve (from a pre-defined family with simple gradient) that
  // best matches an offset exponential decay.
  private static float[] find_ab_params(float spread, float min_dist) {
    /*
    float[] xv = MathUtils.linspace(0, spread * 3, 300);
    float[] yv = new float[xv.length];
    //  yv[xv < min_dist] = 1.0;
    //  yv[xv >= min_dist] = Math.exp(-(xv[xv >= min_dist] - min_dist) / spread   );
    for (int k = 0; k < yv.length; ++k) {
      if (xv[k] < min_dist) {
        yv[k] = 1.0F;
      } else {
        yv[k] = (float) Math.exp(-(xv[k] - min_dist) / spread);
      }
    }
    final float[] params = curve_fit(curve, xv, yv); // todo here curve is the function above -- curve_fit in scipy
    return new float[]{params[0], params[1]};
    */
    if (spread == 1.0F && min_dist == 0.1F) {
      return new float[]{1.57694346F, 0.89506088F};
    }
    throw new UnsupportedOperationException();
  }

  private boolean mAngularRpForest = false;
  private String init = "spectral";
  private int n_neighbors = 15;
  private int n_components = 2;
  private Integer n_epochs = null;
  private Metric mMetric = EuclideanMetric.SINGLETON;
  private float mLearningRate = 1.0F;
  private float mRepulsionStrength = 1.0F;
  private float mMinDist = 0.1F;
  private float mSpread = 1.0F;
  private float mSetOpMixRatio = 1.0F;
  private float mLocalConnectivity = 1.0F;
  private int mNegativeSampleRate = 5;
  private float mTransformQueueSize = 4.0F;
  private Metric mTargetMetric = CategoricalMetric.SINGLETON;
  private int mTargetNNeighbors = -1;
  private float mTargetWeight = 0.5F;
  private int mTransformSeed = 42;
  private boolean mVerbose = false;
  private Float a = null;
  private Float b = null;
  private Random mRandom = new Random(42); // todo seed

  private float mInitialAlpha;
  private int _n_neighbors;
  private boolean mSparseData;
  private float _a;
  private float _b;
  private Matrix mRawData;
  private CsrMatrix mSearchGraph;
  private int[][] _knn_indices;
  private float[][] _knn_dists;
  private List<FlatTree> mRpForest;
  private boolean mSmallData;
  private Metric _distance_func;
  private Matrix graph_;
  private Matrix mEmbedding;
  private NearestNeighborSearch _search;
  private NearestNeighborRandomInit _random_init;
  private NearestNeighborTreeInit _tree_init;

  public void setInit(final String init) {
    this.init = init;
  }

  public void setNumberNearestNeighbours(final int nNeighbors) {
    this.n_neighbors = nNeighbors;
  }

  public void setNumberComponents(final int nComponents) {
    this.n_components = nComponents;
  }

  public void setNumberEpochs(final int nEpochs) {
    this.n_epochs = nEpochs;
  }

  public void setMetric(final Metric metric) {
    mMetric = metric;
  }

  public void setMetric(final String metric) {
    setMetric(Metric.getMetric(metric));
  }

  public void setLearningRate(final float rate) {
    mLearningRate = rate;
  }

  public void setRepulsionStrength(final float repulsion_strength) {
    this.mRepulsionStrength = repulsion_strength;
  }

  public void setMinDist(final float min_dist) {
    this.mMinDist = min_dist;
  }

  public void setSpread(final float spread) {
    this.mSpread = spread;
  }

  public void setSetOpMixRatio(final float setOpMixRatio) {
    mSetOpMixRatio = setOpMixRatio;
  }

  public void setLocalConnectivity(final float localConnectivity) {
    mLocalConnectivity = localConnectivity;
  }

  public void setNegativeSampleRate(final int negativeSampleRate) {
    mNegativeSampleRate = negativeSampleRate;
  }

  public void setTargetMetric(final Metric targetMetric) {
    mTargetMetric = targetMetric;
  }

  public void setTargetMetric(final String targetMetric) {
    setTargetMetric(Metric.getMetric(targetMetric));
  }

  public void setVerbose(final boolean verbose) {
    mVerbose = verbose;
  }

  public void setRandom(final Random random) {
    this.mRandom = random;
  }

  public void setTransformQueueSize(final float transformQueueSize) {
    mTransformQueueSize = transformQueueSize;
  }

  public void setAngularRpForest(final boolean angularRpForest) {
    mAngularRpForest = angularRpForest;
  }

  public void setTargetNNeighbors(final int target_n_neighbors) {
    this.mTargetNNeighbors = target_n_neighbors;
  }

  public void setTargetWeight(final float target_weight) {
    this.mTargetWeight = target_weight;
  }

  public void setTransformSeed(final int transformSeed) {
    mTransformSeed = transformSeed;
  }

  private void validateParameters() {
    if (mSetOpMixRatio < 0.0 || mSetOpMixRatio > 1.0) {
      throw new IllegalArgumentException("set_op_mix_ratio must be between 0.0 and 1.0");
    }
    if (mRepulsionStrength < 0.0) {
      throw new IllegalArgumentException("repulsion_strength cannot be negative");
    }
    if (mMinDist > mSpread) {
      throw new IllegalArgumentException("min_dist must be less than || equal to spread");
    }
    if (mMinDist < 0.0) {
      throw new IllegalArgumentException("min_dist must be greater than 0.0");
    }
//    if (!isinstance(init, str) && !isinstance(init, np.ndarray)) {
//      throw new IllegalArgumentException("init must be a string || ndarray");
//    }
    if (!"spectral".equals(init) && !"random".equals(init)) {
      throw new IllegalArgumentException("string init values must be 'spectral' || 'random'");
    }
//    if (isinstance(init, np.ndarray) && init.shape[1] != n_components) {
//      throw new IllegalArgumentException("init ndarray must match n_components value");
//    }
    if (mNegativeSampleRate < 0) {
      throw new IllegalArgumentException("negative sample rate must be positive");
    }
    if (mInitialAlpha < 0.0) {
      throw new IllegalArgumentException("learning_rate must be positive");
    }
    if (n_neighbors < 2) {
      throw new IllegalArgumentException("n_neighbors must be greater than 2");
    }
    if (mTargetNNeighbors < 2 && mTargetNNeighbors != -1) {
      throw new IllegalArgumentException("target_n_neighbors must be greater than 2");
    }
    if (n_components < 1) {
      throw new IllegalArgumentException("n_components must be greater than 0");
    }
    if (n_epochs != null && n_epochs <= 10) {
      throw new IllegalArgumentException("n_epochs must be a positive integer larger than 10");
    }
  }

  // Fit instances into an embedded space.

  // Optionally use y for supervised dimension reduction.

  // Parameters
  // ----------
  // X : array, shape (n_samples, n_features) || (n_samples, n_samples)
  //     If the metric is 'precomputed' X must be a square distance
  //     matrix. Otherwise it contains a sample per row. If the method
  //     is 'exact', X may be a sparse matrix of type 'csr', 'csc'
  //     || 'coo'.

  // y : array, shape (n_samples)
  //     A target array for supervised dimension reduction. How this is
  //     handled is determined by parameters UMAP was instantiated with.
  //     The relevant attributes are ``target_metric`` and
  //     ``target_metric_kwds``.
  private void fit(Matrix instances, float[] y) {

    //X = check_array(X, dtype = np.float32, accept_sparse = "csr");
    this.mRawData = instances;

    // Handle all the optional arguments, setting default
    if (this.a == null || this.b == null) {
      final float[] ab = find_ab_params(mSpread, mMinDist);
      _a = ab[0];
      _b = ab[1];
    } else {
      _a = this.a;
      _b = this.b;
    }

//      if (isinstance(this.init, np.ndarray)) {
//        init = check_array(this.init,        /*  dtype = */np.float32,         /* accept_sparse =*/ false);
//      } else {
//        init = this.init;
//      }
    String init = this.init;

    this.mInitialAlpha = this.mLearningRate;

    this.validateParameters();

    // Error check n_neighbors based on data size
    if (instances.rows() <= this.n_neighbors) {
      if (instances.rows() == 1) {
        mEmbedding = new DefaultMatrix(new float[1][this.n_components]); // MathUtils.zeros((1, this.n_components) );  // needed to sklearn comparability
        return;
      }

      Utils.message("n_neighbors is larger than the dataset size; truncating to X.length - 1");
      _n_neighbors = instances.rows() - 1;
    } else {
      _n_neighbors = this.n_neighbors;
    }

    if (instances instanceof CsrMatrix) {   // scipy.sparse.isspmatrix_csr(X)) {
      final CsrMatrix csrInstances = (CsrMatrix) instances;
      if (!csrInstances.has_sorted_indices()) {
        csrInstances.sort_indices();
      }
      mSparseData = true;
    } else {
      mSparseData = false;
    }

    final Random random = this.mRandom;

    if (mVerbose) {
      Utils.message("Construct fuzzy simplicial set: " + instances.rows());
    }

    // Handle small cases efficiently by computing all distances
    if (instances.rows() < SMALL_PROBLEM_THRESHOLD) {
      mSmallData = true;
      final Matrix dmat = PairwiseDistances.pairwiseDistances(instances, mMetric);
      graph_ = fuzzySimplicialSet(dmat, _n_neighbors, random, PrecomputedMetric.SINGLETON, null, null, mAngularRpForest, mSetOpMixRatio, mLocalConnectivity, mVerbose);
      // System.out.println("graph: " + ((CooMatrix) graph_).sparseToString());
    } else {
      mSmallData = false;
      // Standard case
      final Object[] nn = nearestNeighbors(instances, _n_neighbors, mMetric, mAngularRpForest, random, mVerbose);
      _knn_indices = (int[][]) nn[0];
      _knn_dists = (float[][]) nn[1];
      mRpForest = (List<FlatTree>) nn[2];

      graph_ = fuzzySimplicialSet(instances, n_neighbors, random, mMetric, _knn_indices, _knn_dists, mAngularRpForest, mSetOpMixRatio, mLocalConnectivity, mVerbose);

      // todo this starts as LilMatrix type but ends up as a CsrMatrix
      // todo this java implementation is not sparse
      // todo according to scipy an efficiency thing -- but bytes (yes because it is actually only storing True/False)
      // todo overall seems to make (0,1)-matrix with a 1 at (x,y) whenever dists(x,y)!=0 or dists(y,x)!=0
//        Matrix tmp_search_graph = //scipy.sparse.lil_matrix((X.rows(), X.rows()), dtype = np.int8      );
//        tmp_search_graph.rows = this._knn_indices;
//        tmp_search_graph.data = (this._knn_dists != 0).astype(np.int8);  // todo what does this do? -- tests each element to be 0, returns True or False for each element
      //Utils.message("knn_indices: " + _knn_indices.length + " " + _knn_indices[0].length + " " + Arrays.toString(_knn_indices[0]));
      //Utils.message("knn_dists: " + _knn_dists.length + " " + _knn_dists[0].length);
      final float[][] tmp_data = new float[instances.rows()][instances.rows()];
      for (int k = 0; k < _knn_indices.length; ++k) {
        for (int j = 0; j < _knn_indices[k].length; ++j) {
          final int i = _knn_indices[k][j];
          tmp_data[k][i] = _knn_dists[k][j] != 0 ? 1.0F : 0.0F;
        }
      }
      final Matrix tmp_matrix = new DefaultMatrix(tmp_data);
      mSearchGraph = tmp_matrix.max(tmp_matrix.transpose()).toCsr();

      _distance_func = mMetric;
      if (mMetric == PrecomputedMetric.SINGLETON) {
        Utils.message("Using precomputed metric; transform will be unavailable for new data");
      } else {
        _random_init = new NearestNeighborRandomInit(_distance_func);
        _tree_init = new NearestNeighborTreeInit(_distance_func);
        _search = new NearestNeighborSearch(_distance_func);
      }
    }

    if (y != null) {
      if (instances.length() != y.length) {
        throw new IllegalArgumentException("Length of x =  " + instances.length() + ", length of y = " + y.length + ", while it must be equal.");
      }
      if (CategoricalMetric.SINGLETON.equals(mTargetMetric)) {
        final float farDist = mTargetWeight < 1.0 ? 2.5F * (1.0F / (1.0F - mTargetWeight)) : 1.0e12F;
        graph_ = categoricalSimplicialSetIntersection((CooMatrix) graph_, y, farDist);
      } else {
        final int targetNNeighbors = this.mTargetNNeighbors == -1 ? _n_neighbors : this.mTargetNNeighbors;

        Matrix targetGraph;
        // Handle the small case as precomputed as before
        if (y.length < SMALL_PROBLEM_THRESHOLD) {
          final Matrix ydmat = PairwiseDistances.pairwiseDistances(MathUtils.promoteTranspose(y), mTargetMetric);
          targetGraph = fuzzySimplicialSet(ydmat, targetNNeighbors, random, PrecomputedMetric.SINGLETON, null, null, false, 1.0F, 1.0F, false);
        } else {
          // Standard case
          targetGraph = fuzzySimplicialSet(MathUtils.promoteTranspose(y), targetNNeighbors, random, mTargetMetric, null, null, false, 1.0F, 1.0F, false);
        }
        graph_ = generalSimplicialSetIntersection(graph_, targetGraph, mTargetWeight);
        graph_ = resetLocalConnectivity(graph_);
      }
    }

    final int nEpochs = this.n_epochs == null ? 0 : this.n_epochs;

    if (mVerbose) {
      Utils.message("Construct embedding");
    }

    mEmbedding = simplicialSetEmbedding(mRawData, graph_, n_components, mInitialAlpha, _a, _b, mRepulsionStrength, mNegativeSampleRate, nEpochs, init, random, mMetric, mVerbose);

    if (mVerbose) {
      Utils.message("Finished embedding");
    }

    //this._input_hash = joblib.hash(this._raw_data);
  }

  // Fit instances into an embedded space and return that transformed
  // output.

  // Parameters
  // ----------
  // instances : array, shape (n_samples, n_features) || (n_samples, n_samples)
  //     If the metric is 'precomputed' X must be a square distance
  //     matrix. Otherwise it contains a sample per row.

  // y : array, shape (n_samples)
  //     A target array for supervised dimension reduction. How this is
  //     handled is determined by parameters UMAP was instantiated with.
  //     The relevant attributes are ``target_metric`` and
  //     ``target_metric_kwds``.

  // Returns
  // -------
  // X_new : array, shape (n_samples, n_components)
  //     Embedding of the training data in low-dimensional space.
  Matrix fitTransform(final Matrix instances, final float[] y) {
    fit(instances, y);
    return this.mEmbedding;
  }

  Matrix fitTransform(final Matrix instances) {
    return fitTransform(instances, null);
  }

  Matrix fitTransform(final float[][] instances) {
    return fitTransform(new DefaultMatrix(instances), null);
  }

  // Transform X into the existing embedded space and return that
  // transformed output.

  // Parameters
  // ----------
  // X : array, shape (n_samples, n_features)
  //     New data to be transformed.

  // Returns
  // -------
  // X_new : array, shape (n_samples, n_components)
  //     Embedding of the new data in low-dimensional space.
  // If we fit just a single instance then error
  private Matrix transform(Matrix X) {
    if (this.mEmbedding.rows() == 1) {
      throw new IllegalArgumentException("Transform unavailable when model was fit with only a single data sample.");
    }
    // If we just have the original input then short circuit things
    //X = check_array(X, dtype = np.float32, accept_sparse = "csr");
    // todo caching of previous run ?
//    int x_hash = joblib.hash(X);
//    if (x_hash == this._input_hash) {
//      return this.embedding_;
//    }

    if (this.mSparseData) {
      throw new IllegalArgumentException("Transform not available for sparse input.");
    } else if (mMetric.equals("precomputed")) {
      throw new IllegalArgumentException("Transform  of new data not available for precomputed metric.");
    }

    int[][] indices;
    float[][] dists;
    if (this.mSmallData) {
      Matrix dmat = PairwiseDistances.pairwiseDistances(X, mRawData, mMetric);
      indices = MathUtils.subArray(MathUtils.argpartition(dmat, _n_neighbors), _n_neighbors);
      float[][] dmatShortened = Utils.submatrix(dmat, indices, _n_neighbors);
      int[][] indicesSorted = MathUtils.argsort(dmatShortened);
      indices = Utils.submatrix(indices, indicesSorted, _n_neighbors);
      dists = Utils.submatrix(dmatShortened, indicesSorted, _n_neighbors);
    } else {
      Heap init = NearestNeighborDescent.initialise_search(mRpForest, mRawData, X, (int) (_n_neighbors * mTransformQueueSize), _random_init, _tree_init, mRandom);
      Heap result = _search.initialized_nnd_search(mRawData, mSearchGraph.indptr, mSearchGraph.indices, init, X);
      result = Utils.deheapSort(result);
      indices = result.indices;
      dists = result.weights;
      indices = MathUtils.subArray(indices, _n_neighbors);
      dists = MathUtils.subArray(dists, _n_neighbors);
    }

    double adjusted_local_connectivity = Math.max(0, mLocalConnectivity - 1.0);
    final float[][] sigmasRhos = smoothKnnDist(dists, _n_neighbors, adjusted_local_connectivity);
    float[] sigmas = sigmasRhos[0];
    float[] rhos = sigmasRhos[1];
    CooMatrix graph = computeMembershipStrengths(indices, dists, sigmas, rhos, new int[]{X.rows(), mRawData.rows()});

    // This was a very specially constructed graph with constant degree.
    // That lets us do fancy unpacking by reshaping the csr matrix indices
    // and data. Doing so relies on the constant degree assumption!
    CsrMatrix csr_graph = (CsrMatrix) Normalize.normalize(graph.toCsr(), "l1");
//    int[][] inds = csr_graph.indices.reshape(X.rows(), this._n_neighbors);
//    float[][] weights = csr_graph.data.reshape(X.rows(), this._n_neighbors);
    // todo following need to be "reshape" as above
    int[][] inds = null;
    float[][] weights = null;
    Matrix embedding = init_transform(inds, weights, mEmbedding);

    final int nEpochs;
    if (this.n_epochs == null) {
      // For smaller datasets we can use more epochs
      if (graph.rows() <= 10000) {
        nEpochs = 100;
      } else {
        nEpochs = 30;
      }
    } else {
      nEpochs = this.n_epochs; // 3.0
    }

    MathUtils.zeroEntriesBelowLimit(graph.mData, MathUtils.max(graph.mData) / (float) nEpochs);
    graph = (CooMatrix) graph.eliminateZeros();

    final float[] epochsPerSample = makeEpochsPerSample(graph.mData, nEpochs);

    int[] head = graph.mRow;
    int[] tail = graph.mCol;

    return optimizeLayout(embedding, mEmbedding.copy(), head, tail, nEpochs, graph.cols(), epochsPerSample, this._a, this._b, mRandom, mRepulsionStrength, mInitialAlpha, mNegativeSampleRate, mVerbose);
  }
}
