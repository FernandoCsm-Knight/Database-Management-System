import java.util.Arrays;

import crud.indexes.trees.BPlusTree;
import crud.indexes.types.NNode;
import logic.SystemSpecification;

public class Main implements SystemSpecification {

    public static void main(String[] args) throws Exception {
        BPlusTree<NNode> tree = new BPlusTree<>(8, "BPlusTree.db", NNode.class.getConstructor());

        tree.toJsonFile();
    }

}
