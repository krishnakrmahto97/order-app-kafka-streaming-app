package com.course.kafka.broker.stream.feedback.rating;

import com.course.kafka.broker.message.FeedbackAverageRatingMessageOne;
import com.course.kafka.broker.message.FeedbackMessage;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.streams.kstream.ValueTransformer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;

import java.util.Optional;

@RequiredArgsConstructor
public class FeedbackRatingTransformerOne implements ValueTransformer<FeedbackMessage, FeedbackAverageRatingMessageOne>
{
    private ProcessorContext processorContext;
    private final String stateStoreName;
    private KeyValueStore<String, FeedbackRatingStateStoreValue> stateStore;

    @Override
    @SuppressWarnings("unchecked")
    public void init(ProcessorContext context)
    {
        processorContext = context;
        stateStore = (KeyValueStore<String, FeedbackRatingStateStoreValue>) processorContext.getStateStore(stateStoreName);
    }

    @Override
    public FeedbackAverageRatingMessageOne transform(FeedbackMessage feedbackMessage)
    {
        String locationInMessage = feedbackMessage.getLocation();

        FeedbackRatingStateStoreValue stateStoreValue = Optional.ofNullable(stateStore.get(locationInMessage))
                                                                .orElseGet(FeedbackRatingStateStoreValue::new);

        long currentTotalSum = feedbackMessage.getRating() + stateStoreValue.getSumRating();
        long currentTotalCount = stateStoreValue.getCount() + 1;

        double averageRating = (double) currentTotalSum / currentTotalCount;

        stateStore.put(locationInMessage, new FeedbackRatingStateStoreValue(currentTotalCount, currentTotalSum));

        return new FeedbackAverageRatingMessageOne(locationInMessage, averageRating);
    }

    @Override
    public void close()
    {

    }
}
