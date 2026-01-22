import { useChat } from "ai/react";
import { Thread } from "@assistant-ui/react";
import { FC } from "react";

interface SinsayChatProps {
    intent: string;
    orderId: string;
    description: string;
    image?: File | null;
}

export const SinsayChat: FC<SinsayChatProps> = ({ intent, orderId, description, image }) => {
    const { messages, input, handleInputChange, handleSubmit } = useChat({
        api: "/api/chat",
        body: {
            intent,
            orderId
        },
        initialMessages: image ? [
            {
                id: 'init-1',
                role: 'user',
                content: description,
                experimental_attachments: image ? [
                    {
                        name: image.name,
                        contentType: image.type,
                        url: URL.createObjectURL(image)
                    }
                ] : undefined
            }
        ] : [
            {
                id: 'init-1',
                role: 'user',
                content: description || "Hello"
            }
        ]
    });

    return (
        <div className="h-[600px] w-full border rounded-md bg-white shadow-sm overflow-hidden">
            <Thread
                messages={messages}
                input={input}
                handleInputChange={handleInputChange}
                handleSubmit={handleSubmit}
                assistantAvatar={{ src: "https://www.sinsay.com/static/assets/favicons/apple-touch-icon.png" }}
                welcome={{
                    message: "Dzień dobry. Jestem wirtualnym asystentem Sinsay. Analizuję Twoje zgłoszenie..."
                }}
            />
        </div>
    );
};
