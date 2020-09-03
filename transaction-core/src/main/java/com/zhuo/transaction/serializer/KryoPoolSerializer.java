package com.zhuo.transaction.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.pool.KryoCallback;
import com.esotericsoftware.kryo.pool.KryoFactory;
import com.esotericsoftware.kryo.pool.KryoPool;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * describe:
 *
 * @author zhuojing
 * @date 2020/09/01
 */
public class KryoPoolSerializer implements ObjectSerializer<Object> {


    static KryoFactory factory = new KryoFactory() {
        @Override
        public Kryo create() {
            Kryo kryo = new Kryo();
            kryo.setReferences(true);
            kryo.setRegistrationRequired(false);
            ((Kryo.DefaultInstantiatorStrategy) kryo.getInstantiatorStrategy())
                    .setFallbackInstantiatorStrategy(new StdInstantiatorStrategy());
            return kryo;
        }
    };


    KryoPool pool = new KryoPool.Builder(factory).softReferences().build();

    private int initPoolSize = 100;

    public KryoPoolSerializer() {
        init();
    }

    public KryoPoolSerializer(int initPoolSize) {
        this.initPoolSize = initPoolSize;
        init();
    }

    private void init() {
        for (int i = 0; i < initPoolSize; i++) {
            Kryo kryo = pool.borrow();
            pool.release(kryo);
        }
    }
    @Override
    public byte[] serialize(final Object object) {

        return pool.run(new KryoCallback<byte[]>() {
            @Override
            public byte[] execute(Kryo kryo) {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                Output output = new Output(byteArrayOutputStream);
                kryo.writeClassAndObject(output, object);
                output.flush();
                return byteArrayOutputStream.toByteArray();
            }
        });
    }

    @Override
    public Object deserialize(final byte[] bytes) {

        return pool.run(new KryoCallback<Object>() {
            @Override
            public Object execute(Kryo kryo) {
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
                Input input = new Input(byteArrayInputStream);

                return kryo.readClassAndObject(input);
            }
        });
    }

    @Override
    public Object clone(final Object object) {
        return pool.run(new KryoCallback<Object>() {
            @Override
            public Object execute(Kryo kryo) {
                return kryo.copy(object);
            }
        });
    }
}
