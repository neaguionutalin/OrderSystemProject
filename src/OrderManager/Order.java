package OrderManager;

import java.io.Serializable;
import java.util.ArrayList;

import Ref.Instrument;

public class Order implements Serializable{
	public int id; //TODO these should all be longs
	short orderRouter;
	public int clientOrderID; //TODO refactor to lowercase C
	int size;
	double[]bestPrices;
	int bestPriceCount;
	int clientId;
	public Instrument instrument;
	public double initialMarketPrice;
	ArrayList<Order>slices;
	ArrayList<Fill>fills;
	char OrdStatus='A'; //OrdStatus is Fix 39, 'A' is 'Pending New'
	//Status state;

	public Order(int clientId, int ClientOrderID, Instrument instrument, int size){
		this.clientOrderID =ClientOrderID;
		this.size=size;
		this.clientId =clientId;
		this.instrument=instrument;
		fills=new ArrayList<Fill>();
		slices=new ArrayList<Order>();
	}

	public int sliceSizes(){
		int totalSizeOfSlices=0;
		for(Order c:slices)totalSizeOfSlices+=c.size;
		return totalSizeOfSlices;
	}
	public int newSlice(int sliceSize){
		slices.add(new Order(id, clientOrderID,instrument,sliceSize));
		return slices.size()-1;
	}
	public int sizeFilled(){
		int filledSoFar=0;
		for(Fill f:fills){
			filledSoFar+=f.size;
		}
		for(Order c:slices){
			filledSoFar+=c.sizeFilled();
		}
		return filledSoFar;
	}
	public int sizeRemaining(){
		return size-sizeFilled();
	}

	float price(){
		//TODO this is buggy as it doesn't take account of slices. Let them fix it
		float sum=0;
		for(Fill fill:fills){
			sum+=fill.price;
		}
		return sum/fills.size();
	}
	void createFill(int size,double price){
		fills.add(new Fill(size,price));
		if(sizeRemaining()==0){
			OrdStatus='2';
		}else{
			OrdStatus='1';
		}
	}
	void cross(Order matchingOrder){
		//pair slices first and then parent
		for(Order slice:slices){
			if(slice.sizeRemaining()==0)continue;
			//TODO could optimise this to not start at the beginning every time
			for(Order matchingSlice:matchingOrder.slices){
				int matchingSliceSize=matchingSlice.sizeRemaining();
				if(matchingSliceSize==0)continue;
				int sliceSize=slice.sizeRemaining();
				if(sliceSize<=matchingSliceSize){
					 slice.createFill(sliceSize,initialMarketPrice);
					 matchingSlice.createFill(sliceSize, initialMarketPrice);
					 break;
				}
				//sliceSize>matchingSliceSize
				slice.createFill(matchingSliceSize,initialMarketPrice);
				matchingSlice.createFill(matchingSliceSize, initialMarketPrice);
			}
			int sliceSize=slice.sizeRemaining();
			int mParent=matchingOrder.sizeRemaining()-matchingOrder.sliceSizes();
			if(sliceSize>0 && mParent>0){
				if(sliceSize>=mParent){
					slice.createFill(sliceSize,initialMarketPrice);
					matchingOrder.createFill(sliceSize, initialMarketPrice);
				}else{
					slice.createFill(mParent,initialMarketPrice);
					matchingOrder.createFill(mParent, initialMarketPrice);					
				}
			}
			//no point continuing if we didn't fill this slice, as we must already have fully filled the matchingOrder
			if(slice.sizeRemaining()>0)break;
		}
		if(sizeRemaining()>0){
			for(Order matchingSlice:matchingOrder.slices){
				int matchingSliceSize=matchingSlice.sizeRemaining();
				if(matchingSliceSize==0)continue;
				int sliceSize=sizeRemaining();
				if(sliceSize<=matchingSliceSize){
					 createFill(sliceSize,initialMarketPrice);
					 matchingSlice.createFill(sliceSize, initialMarketPrice);
					 break;
				}
				//sliceSize>matchingSliceSize
				createFill(matchingSliceSize,initialMarketPrice);
				matchingSlice.createFill(matchingSliceSize, initialMarketPrice);
			}
			int sliceSize=sizeRemaining();
			int mParent=matchingOrder.sizeRemaining()-matchingOrder.sliceSizes();
			if(sliceSize>0 && mParent>0){
				if(sliceSize>=mParent){
					createFill(sliceSize,initialMarketPrice);
					matchingOrder.createFill(sliceSize, initialMarketPrice);
				}else{
					createFill(mParent,initialMarketPrice);
					matchingOrder.createFill(mParent, initialMarketPrice);					
				}
			}
		}
	}
	void cancel(){
		//state=cancelled
	}

	public char getOrdStatus(){
		return OrdStatus;
	}

	public void setOrdStatus(char OrdStatus){
		this.OrdStatus = OrdStatus;
	}
}

