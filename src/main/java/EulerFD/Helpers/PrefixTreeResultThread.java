package EulerFD.Helpers;

import EulerFD.Bitset.IBitSet;
import EulerFD.Bitset.search.TreeSearch;
import EulerFD.Bitset.LongBitSet;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class PrefixTreeResultThread {

	private final Set<IBitSet> invalid = new HashSet<IBitSet>();
	private final Set<IBitSet> invalid_new = new HashSet<IBitSet>();
	private final int numberAttributes;
	private int lhsLength;
	private int newNonFd;
	private IBitSet constantColumns;
	private Integer[] indexes;
	private TreeSearch[] posCoverTrees;
	private ResultController resultController;

	public PrefixTreeResultThread(int numberAttributes, ResultController resultController) {
		this.numberAttributes = numberAttributes;
		this.resultController = resultController;
		this.lhsLength = 0;
		this.newNonFd = 0;
		this.posCoverTrees = new TreeSearch[numberAttributes];
		for (int i = 0; i < numberAttributes; i++ ){
			this.posCoverTrees[i] = mostGeneralFDs(i);
		}
	}

	public void add(IBitSet set) {
		int old_size = invalid.size();
		invalid.add(set);
		if (old_size != invalid.size()) {
			invalid_new.add(set);
			lhsLength += set.cardinality();
			newNonFd += set.length() - set.cardinality();
		}
	}

	public void printSize(){
		System.out.println("New Size : " + invalid_new.size() + " Total Size: " + invalid.size());
	}

	public void clear(){
		invalid_new.clear();
	}

	public int generateResults()  {
		int[] counts = new int[numberAttributes];
		invalid.stream().forEach(
				bitset -> {
					for (int i = bitset.nextSetBit(0); i >= 0; i = bitset.nextSetBit(i + 1)) {
						counts[i]++;
					}
				});
		ArrayIndexComparator comparator = new ArrayIndexComparator(counts, ArrayIndexComparator.Order.ASCENDING);
		indexes = comparator.createIndexArray();
		Arrays.sort(indexes, comparator);

		int[] invIndexes = new int[numberAttributes];
		for (int i = 0; i < numberAttributes; ++i) {
			invIndexes[indexes[i].intValue()] = i;
		}

		final ArrayList<IBitSet> sortedNegCover = new ArrayList<IBitSet>();
		invalid.stream().forEach(bitset -> {
			IBitSet bitset2 = LongBitSet.FACTORY.create();
			for (Integer i : indexes) {
				if (bitset.get(indexes[i.intValue()].intValue())) {
					bitset2.set(i.intValue());
				}
			}
			sortedNegCover.add(bitset2);
		});

		Collections.sort(sortedNegCover, new Comparator<IBitSet>() {
			@Override
			public int compare(IBitSet o1, IBitSet o2) {
				int erg = Integer.compare(o2.cardinality(), o1.cardinality());
				return erg != 0 ? erg : o2.compareTo(o1);
			}
		});
		List<IBitSet> res = new ArrayList<>();
		int fds_num = 0;
		for (int target = 0; target < numberAttributes; ++target) {
			if(constantColumns.get(target))
				continue;
			final int targetF = invIndexes[target];
			TreeSearch neg = new TreeSearch();
			sortedNegCover.stream()
					.filter(invalidFD -> !invalidFD.get(targetF))
					.forEach(invalid -> addInvalidToNeg(neg, invalid));

			TreeSearch posCover = posCoverTrees[targetF];

			final ArrayList<IBitSet> list = new ArrayList<IBitSet>();
			neg.forEach(invalidFD -> list.add(invalidFD));
			Collections.sort(list, new Comparator<IBitSet>() {
				@Override
				public int compare(IBitSet o1, IBitSet o2) {
					int erg = Integer.compare(o2.cardinality(),
							o1.cardinality());
					return erg != 0 ? erg : o2.compareTo(o1);
				}
			});

			list.forEach(invalidFD -> handleInvalid(invalidFD, posCover, targetF));

			final int finalTarget = target;
			posCover.forEach(bitset -> {
				IBitSet valid = LongBitSet.FACTORY.create();
				for (int i = bitset.nextSetBit(0); i >= 0; i = bitset
						.nextSetBit(i + 1)) {
					valid.set(indexes[i].intValue());
				}
				this.resultController.add(valid, finalTarget);
			});
			fds_num += posCover.fds_num;
		}
		return fds_num;
	}

	public int generateResultsThreads()  {
		int[] counts = new int[numberAttributes];
		invalid.stream().forEach(
			bitset -> {
				for (int i = bitset.nextSetBit(0); i >= 0; i = bitset.nextSetBit(i + 1)) {
					counts[i]++;
				}
			});
		ArrayIndexComparator comparator = new ArrayIndexComparator(counts, ArrayIndexComparator.Order.ASCENDING);
		indexes = comparator.createIndexArray();
		Arrays.sort(indexes, comparator);

		int[] invIndexes = new int[numberAttributes];
		for (int i = 0; i < numberAttributes; ++i) {
			invIndexes[indexes[i].intValue()] = i;
		}

		final ArrayList<IBitSet> sortedNegCover = new ArrayList<IBitSet>();
		invalid.stream().forEach(bitset -> {
			IBitSet bitset2 = LongBitSet.FACTORY.create();
			for (Integer i : indexes) {
				if (bitset.get(indexes[i.intValue()].intValue())) {
					bitset2.set(i.intValue());
				}
			}
			sortedNegCover.add(bitset2);
		});

		Collections.sort(sortedNegCover, new Comparator<IBitSet>() {
			@Override
			public int compare(IBitSet o1, IBitSet o2) {
				int erg = Integer.compare(o2.cardinality(), o1.cardinality());
				return erg != 0 ? erg : o2.compareTo(o1);
			}
		});
		List<IBitSet> res = new ArrayList<>();
		final int[] fds_num = {0};
		ThreadPoolExecutor service = new ThreadPoolExecutor(8, 225,
		0L, TimeUnit.MICROSECONDS,
		new LinkedBlockingDeque<>(100),
		new ThreadFactoryBuilder().setNameFormat("demo").build(),
		new ThreadPoolExecutor.AbortPolicy());

		for (int target = 0; target < numberAttributes; ++target) {
			if(constantColumns.get(target))
				continue;
			int finalTarget = target;
			service.execute(new Runnable() {
				@Override
				public void run() {
					final int targetF = invIndexes[finalTarget];
					TreeSearch neg = new TreeSearch();
					sortedNegCover.stream()
							.filter(invalidFD -> !invalidFD.get(targetF))
							.forEach(invalid -> addInvalidToNeg(neg, invalid));

					TreeSearch posCover = posCoverTrees[targetF];

					final ArrayList<IBitSet> list = new ArrayList<IBitSet>();
					neg.forEach(invalidFD -> list.add(invalidFD));
					Collections.sort(list, new Comparator<IBitSet>() {
						@Override
						public int compare(IBitSet o1, IBitSet o2) {
							int erg = Integer.compare(o2.cardinality(),
									o1.cardinality());
							return erg != 0 ? erg : o2.compareTo(o1);
						}
					});

					list.forEach(invalidFD -> handleInvalid(invalidFD, posCover, targetF));

					posCover.forEach(bitset -> {
						IBitSet valid = LongBitSet.FACTORY.create();
						for (int i = bitset.nextSetBit(0); i >= 0; i = bitset
								.nextSetBit(i + 1)) {
							valid.set(indexes[i].intValue());
						}
					});
					fds_num[0] += posCover.fds_num;
				}
			});
		}
		ThreadPoolExecutor tpe = (ThreadPoolExecutor) service;
		while(true){
			if (tpe.getActiveCount() == 0) break;
		}
		service.shutdown();
		return fds_num[0];
	}

	private void IndexToRank(TreeSearch posCover, int invIndex[]){
		posCover.forEach(bitset -> {
			IBitSet valid = LongBitSet.FACTORY.create();
			for (int i = bitset.nextSetBit(0); i >= 0; i = bitset.nextSetBit(i + 1)){
				valid.set(invIndex[i]);
			}
		});
	}

	public int getNewFdSize(){
		int num = this.invalid_new.size();
		this.invalid_new.clear();
		return num;
	}

	public int getNewNonFd() {
		int num = newNonFd;
		newNonFd = 0;
		return num;
	}

	public int getFdLength(){
		int len = this.lhsLength;
		this.lhsLength = 0;
		return len;
	}

	public int getNegCoverSize() {
		return invalid.size();
	}

	private void addInvalidToNeg(TreeSearch neg, IBitSet invalid) {
		if (neg.findSuperSet(invalid) != null)
			return;
		getAndRemoveGeneralizations(neg, invalid);

		neg.add(invalid);
	}

	private TreeSearch mostGeneralFDs(int target) {
		TreeSearch tree = new TreeSearch();
		for (int single = 0; single < numberAttributes; ++single) {
			if (single != target ) {
				IBitSet bs = LongBitSet.FACTORY.create();
				bs.set(single);
				tree.add(bs);
			}

		}
		return tree;
	}

	private void handleInvalid(IBitSet invalidFD, TreeSearch tree, int target) {
		Set<IBitSet> remove = getAndRemoveGeneralizations(tree, invalidFD);
		for (IBitSet removed : remove) {
			for (int i = 0; i < numberAttributes; ++i) {
				if (i == target || invalidFD.get(i) || constantColumns.get(indexes[i].intValue()))
					continue;
				IBitSet add = LongBitSet.FACTORY.create(removed);
				add.set(i);
				if (tree.findSubSet(add) == null) {
					tree.add(add);
				}
			}
		}
	}

	private void removeGenerliazations(TreeSearch tree, IBitSet invalidFD){
		final Set<IBitSet> remove = new HashSet<>();
		tree.forEachSubSet(invalidFD, t -> remove.add(t));
		remove.forEach(i -> tree.remove(i));
	}

	private Set<IBitSet> getAndRemoveGeneralizations(TreeSearch tree, IBitSet invalidFD) {
		final Set<IBitSet> remove = new HashSet<IBitSet>();
		tree.forEachSubSet(invalidFD, t -> remove.add(t));
		remove.forEach(i -> tree.remove(i));
		return remove;
	}

	public void setConstantColumns(IBitSet constantColumns) {
		this.constantColumns = constantColumns;
	}
}

